(ns starcity.api.mars.rent.bank-account.setup
  (:require [blueprints.models.account :as account]
            [blueprints.models.customer :as customer]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.spec :as s]
            [clojure.string :as str]
            [compojure.core :refer [defroutes GET POST]]
            [datomic.api :as d]
            [ribbon.customer :as rcu]
            [starcity.config :as config :refer [config]]
            [starcity.countries :as countries]
            [starcity.datomic :refer [conn]]
            [starcity.models.autopay :as autopay]
            [starcity.services.plaid :as plaid]
            [starcity.util.request :as req]
            [starcity.util.response :as res]
            [taoensso.timbre :as timbre]
            [toolbelt.async :refer [<!!?]]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]
            [toolbelt.predicates :as p]
            [ribbon.charge :as rc]
            [reactor.events :as events]
            [starcity.util.validation :as validation]))

;; =============================================================================
;; Handlers
;; =============================================================================


;;; Helpers


(defn- create-customer! [account token verified]
  (let [cus (<!!? (rcu/create! (config/stripe-private-key config)
                               (account/email account)
                               token))]
    (if verified
      (customer/create (:id cus) account :bank-token token)
      (customer/create (:id cus) account))))


(defn- add-bank-account!
  ([db account token]
   (add-bank-account! db account token false))
  ([db account token verified]
   (try
     (if-let [customer (customer/by-account db account)]
       ;; customer exists, add source to it
       (do
         (<!!? (rcu/add-source! (config/stripe-private-key config)
                                (customer/id customer)
                                token))
         (assoc {:db/id (:db/id customer)} :stripe-customer/bank-account-token token))
       ;; no customer exists, create one w/ source
       (create-customer! account token verified))
     (catch Exception e
       (timbre/error e ::add-account {:account (account/email account)})
       (throw e)))))

(s/fdef add-bank-account!
        :args (s/cat :db p/db?
                     :account p/entity?
                     :token string?
                     :verified (s/? (s/or :bool boolean? :keyword keyword?)))
        :ret map?)


;;; Initialization


(defn- init
  "Provides required information for the client to bootstrap itself depending on
  requesting user's account info."
  [req]
  (let [account (req/requester (d/db conn) req)]
    (res/json-ok {:stripe    {:public-key (config/stripe-public-key config)}
                  :plaid     {:env        (config/plaid-env config)
                              :public-key (config/plaid-public-key config)}
                  :countries countries/countries
                  :setup     (autopay/setup (d/db conn) account)})))


;;; Plaid


(defn- public-token->bank-token [public-token account-id]
  (get-in (plaid/exchange-token public-token :account-id account-id)
          [:body :stripe_bank_account_token]))


(defn plaid-verify
  "Handler used to verify a bank account using Plaid."
  [{:keys [params] :as req}]
  (let [account                           (req/requester (d/db conn) req)
        {:keys [public-token account-id]} params]
    (cond
      (str/blank? account-id)   (res/json-malformed {:error "A bank account id is required."})
      (str/blank? public-token) (res/json-malformed {:error "A public token is requred."})
      :otherwise                (let [token (public-token->bank-token public-token account-id)]
                                  @(d/transact conn [(add-bank-account! (d/db conn) account token :verified)])
                                  (res/json-ok {:status (autopay/setup-status (d/db conn) account)})))))


;;; Microdeposits


(defn submit-bank-info
  "The first stage in manual verification of a bank account is to create a
  customer object on Stripe. This will hold reference to the bank account, which
  will later be verified once the microdeposits have been made and submitted by
  customer."
  [{:keys [params] :as req}]
  (let [account (req/requester (d/db conn) req)]
    (if-let [token (:stripe-token params)]
      (do
        @(d/transact conn [(add-bank-account! (d/db conn) account token)])
        (res/json-ok {:status (autopay/setup-status (d/db conn) account)}))
      (res/json-malformed {:error "No token submitted."}))))


(defn- deposits-valid? [[amount-1 amount-2]]
  (let [rules [[v/required :message "Both deposits are required."]
               [v/integer :message "Please enter the deposit amounts in cents (whole numbers only)."]
               [v/in-range [0 100] :message "Please enter numbers between 1 and 100."]]]
    (b/validate
     {:deposit-1 amount-1
      :deposit-2 amount-2}
     {:deposit-1 rules
      :deposit-2 rules})))


(defn get-bank-token [customer]
  (let [cus (<!!? (rcu/fetch (config/stripe-private-key config)
                             (customer/id customer)))]
    (:id (rcu/unverified-bank-account cus))))


(defn verify-deposits
  [{:keys [params] :as req}]
  (let [account  (req/requester (d/db conn) req)
        stripe   (config/stripe-private-key config)
        deposits (:deposits params)
        customer (customer/by-account (d/db conn) account)
        cus      (<!!? (rcu/fetch stripe (customer/id customer)))
        vresult  (deposits-valid? deposits)]
    (cond
      (not (validation/valid? vresult))
      (res/json-malformed {:error (first (validation/errors vresult))})

      (rcu/verification-failed? cus)
      (let [sid (:id (tb/find-by rcu/failed-bank-account? (rcu/bank-accounts cus)))]
        @(d/transact conn [(events/delete-source (customer/id customer) sid)])
        (res/json-malformed {:error "Maximum number of verification attempts exceeded. Please refresh the page and try again."}))

      :otherwise
      (try
        (let [cus (customer/by-account (d/db conn) account)
              bat (get-bank-token cus)
              res (<!!? (rcu/verify-bank-account! (config/stripe-private-key config)
                                                  (customer/id cus)
                                                  bat
                                                  (first deposits)
                                                  (second deposits)))]
          (timbre/info ::verify-microdeposits {:account  (account/email account)
                                               :customer (customer/id cus)})
          @(d/transact conn [(assoc {:db/id (td/id cus)} :stripe-customer/bank-account-token bat)])
          (res/json-ok {:status (autopay/setup-status (d/db conn) account)}))
        (catch Exception e
          (timbre/error e ::verify-microdeposits {:account (account/email account)})
          (res/json-unprocessable {:error (get (ex-data e) :message)}))))))


;; =============================================================================
;; Routes
;; =============================================================================


(defroutes routes
  (GET "/" [] init)
  (POST "/plaid/verify" [] plaid-verify)
  (POST "/deposits" [] submit-bank-info)
  (POST "/deposits/verify" [] verify-deposits))
