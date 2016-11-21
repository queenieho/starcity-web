(ns starcity.models.stripe
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when]]
            [starcity spec
             [datomic :refer [conn tempid]]]
            [starcity.models
             [account :as account]
             [util :refer :all]]
            [starcity.services.stripe :as service]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- customer-sources
  [customer]
  (get-in customer [:sources :data]))

(defn- customer-for-account
  [account]
  (:stripe-customer/customer-id
   (one (d/db conn) :stripe-customer/account (:db/id account))))

;; =============================================================================
;; API
;; =============================================================================

(s/def ::id string?)
(s/def ::object #{"customer"})
(s/def ::customer
  (s/keys :req-un [::id ::object]))

;; =============================================================================
;; Selectors

(def fetch-customer
  (comp :body service/fetch-customer))

(s/fdef fetch-customer
        :args (s/cat :customer-id string?)
        :ret ::customer)

(defn bank-accounts
  [customer]
  (filter #(= "bank_account" (:object %)) (customer-sources customer)))

(s/fdef bank-accounts
        :args (s/cat :customer ::customer))

(defn bank-account
  [customer]
  (first (bank-accounts customer)))

(s/fdef bank-account
        :args (s/cat :customer ::customer))

(def bank-account-token
  (comp :id bank-account))

(s/fdef bank-account-token
        :args (s/cat :customer ::customer))

;; =============================================================================
;; Predicates

(defn bank-account-verified?
  [customer]
  (= (:status (bank-account customer)) "verified"))

(s/fdef bank-account-verified?
        :args (s/cat :customer ::customer)
        :ret boolean?)

(defn verification-failed?
  [customer]
  (every?
   (fn [{status :status}] (= status "verification_failed"))
   (bank-accounts customer)))

(s/fdef verification-failed?
        :args (s/cat :customer ::customer)
        :ret boolean?)

;; =============================================================================
;; Actions

(defn create-customer!
  "Create a customer on Stripe with given token and create a corresponding
  entity in our DB."
  [account token]
  (let [res (service/create-customer (account/email account) token)]
    (if-let [error (service/error-from res)]
      ;; Error while creating customer!
      (throw (ex-info "Error encountered while trying to create Stripe customer." error))
      ;; Successful creation of customer
      (let [customer (service/payload-from res)
            tid      (tempid)
            tx       @(d/transact conn [{:db/id                       tid
                                         :stripe-customer/customer-id (:id customer)
                                         :stripe-customer/account     (:db/id account)}])]
        {:entity   (d/entity (d/db conn) (d/resolve-tempid (d/db conn) (:tempids tx) tid))
         :customer customer}))))

(s/def ::entity :starcity.spec/entity)
(s/fdef create-customer!
        :args (s/cat :account :starcity.spec/entity
                     :token string?)
        :ret (s/keys :req-un [::customer ::entity]))

(defn delete-customer
  "Deletes a Stripe customer."
  [account]
  (let [stripe-customer (one (d/db conn) :stripe-customer/account (:db/id account))
        res             (service/delete-customer (:stripe-customer/customer-id stripe-customer))]
    (if-let [error (service/error-from res)]
      (throw (ex-info "Error encountered while trying to delete Stripe customer." error))
      @(d/transact conn [[:db.fn/retractEntity (:db/id stripe-customer)]]))))

(s/fdef delete-customer
        :args (s/cat :account :starcity.spec/entity))

(defn verify-microdeposits
  "Attempt to verify the microdeposit amounts for given `account-id`. Successful
  verification results in the `:stripe-customer/bank-account-token` attribute being
  added to the `:stripe-customer` entity."
  [account deposit-1 deposit-2]
  (let [cid      (customer-for-account account)
        customer (fetch-customer cid)
        bid      (bank-account-token customer)
        res      (service/verify-source cid bid deposit-1 deposit-2)]
    (if-let [e (service/error-from res)]
      (throw (ex-info "Error encountered while trying to verify source!" e))
      (do
        @(d/transact conn [{:stripe-customer/bank-account-token bid
                            :db/id                              [:stripe-customer/customer-id cid]}])
        (service/payload-from res)))))

(defn create-charge!
  "Attempt to create a Stripe charge for given `account-id`. Successful creation
  results in creation of a corresponding `charge`, otherwise an exception is thrown."
  [account-id amount source & {:keys [description customer-id managed-account]}]
  (let [email (:account/email (d/entity (d/db conn) account-id))
        res   (service/charge amount source email
                              :description description
                              :customer-id customer-id
                              :managed-account managed-account)]
    (if-let [e (service/error-from res)]
      (throw (ex-info "Error encountered while trying to create charge!" e))
      (let [payload   (service/payload-from res)
            tid       (tempid)
            tx        @(d/transact conn [(assoc-when
                                          {:db/id            tid
                                           :charge/stripe-id (:id payload)
                                           :charge/account   account-id
                                           :charge/status    :charge.status/pending}
                                          :charge/purpose description)])
            charge-id (d/resolve-tempid (d/db conn) (:tempids tx) tid)]
        charge-id))))

(s/fdef create-charge!
        :args (s/cat :account-id :starcity.spec/lookup
                     :amount pos-int?
                     :source string?
                     :opts (s/keys* :opt-un [::customer-id ::description ::managed-account]))
        :ret integer?)

(defn fetch-charge
  "Fetch a charge from Stripe's servers."
  [{:keys [:charge/stripe-id]}]
  (let [res (service/fetch-charge stripe-id)]
    (if-let [err (service/error-from res)]
      (throw (ex-info "Error encountered while fetching charge!" err))
      (service/payload-from res))))

;; =============================================================================
;; Misc.

;; TODO: Janky? Possible to remove? Only used in one place...
(defn exception-msg
  [e]
  (-> e ex-data :message))
