(ns starcity.api.mars.rent.bank-account.setup
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [clojure.string :as str]
            [compojure.core :refer [defroutes GET POST]]
            [starcity
             [auth :as auth]
             [countries :as countries]
             [datomic :refer [conn]]]
            [starcity.api.common :refer :all]
            [starcity.config
             [plaid :as pc]
             [stripe :as sc]]
            [starcity.models
             [account :as account]
             [autopay :as autopay]]
            [starcity.models.stripe.customer :as customer]
            [starcity.services.plaid :as service]
            [taoensso.timbre :as timbre]
            [datomic.api :as d]
            [starcity.services.stripe.sources :as sources]))

;;; Helpers

(defn- add-bank-account! [conn account token]
  (try
    (if-let [customer (account/stripe-customer (d/db conn) account)]
      (do
        (sources/create! (customer/id customer) token)
        @(d/transact conn [{:db/id                              (:db/id customer)
                            :stripe-customer/bank-account-token token}]))
      (customer/create-platform! account token))
    (catch Exception e
      (timbre/error e ::add-account {:account (account/email account)})
      (throw e))))

;;; Initialization

(defn- init
  "Provides required information for the client to bootstrap itself depending on
  requesting user's account info."
  [req]
  (let [account (auth/requester req)]
    (ok {:stripe    {:public-key sc/public-key}
         :plaid     {:env        pc/env
                     :public-key pc/public-key}
         :countries countries/countries
         :setup     (autopay/setup conn account)})))

;;; Plaid Verification

(defn- public-token->bank-token [public-token account-id]
  (get-in (service/exchange-token public-token :account-id account-id)
          [:body :stripe_bank_account_token]))

(defn plaid-verify
  "Handler used to verify a bank account using Plaid."
  [{:keys [params] :as req}]
  (let [account                           (auth/requester req)
        {:keys [public-token account-id]} params]
    (cond
      (str/blank? account-id)   (malformed {:error "A bank account id is required."})
      (str/blank? public-token) (malformed {:error "A public token is requred."})
      :otherwise                (let [token (public-token->bank-token public-token account-id)]
                                  (add-bank-account! conn account token)
                                  (ok {:status (autopay/setup-status conn account)})))))

;;; Manual verification w/ Microdeposits

(defn submit-bank-info
  "The first stage in manual verification of a bank account is to create a
  customer object on Stripe. This will hold reference to the bank account, which
  will later be verified once the microdeposits have been made and submitted by
  customer."
  [{:keys [params] :as req}]
  (let [account (auth/requester req)]
    (if-let [token (:stripe-token params)]
      (do
        (add-bank-account! conn account token)
        (ok {:status (autopay/setup-status conn account)}))
      (malformed {:error "No token submitted."}))))

;; TODO: Duplicated in starcity.controllers.onboarding
;; Need a better validation workflow.
(defn- deposits-valid? [[amount-1 amount-2]]
  (let [rules [[v/required :message "Both deposits are required."]
               [v/integer :message "Please enter the deposit amounts in cents (whole numbers only)."]
               [v/in-range [0 100] :message "Please enter numbers between 1 and 100."]]]
    (b/validate
     {:deposit-1 amount-1
      :deposit-2 amount-2}
     {:deposit-1 rules
      :deposit-2 rules})))

(defn verify-deposits
  [{:keys [params] :as req}]
  (let [account  (auth/requester req)
        deposits (:deposits params)]
    (if-not (deposits-valid? deposits)
      (malformed {:error "Invalid deposit amounts."})
      (try
        (let [res (customer/verify-microdeposits (account/stripe-customer (d/db conn) account)
                                                 (first deposits)
                                                 (second deposits))]
          (timbre/info ::verify-microdeposits {:account     (account/email account)
                                               :customer-id (:customer res)})
          (ok {:status (autopay/setup-status conn account)}))
        (catch Exception e
          (timbre/error e ::verify-microdeposits {:account (account/email account)})
          (throw e))))))

;;; Routes

(defroutes routes
  (GET "/" [] init)
  (POST "/plaid/verify" [] plaid-verify)
  (POST "/deposits" [] submit-bank-info)
  (POST "/deposits/verify" [] verify-deposits))
