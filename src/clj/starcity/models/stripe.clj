(ns starcity.models.stripe
  (:require [starcity.services.stripe :as service]
            [starcity.datomic :refer [conn tempid]]
            [starcity.models.util :refer :all]
            [datomic.api :as d]
            [clojure.spec :as s]
            [starcity.spec]
            [clojure.java.io :as io]
            [plumbing.core :refer [assoc-when]]
            [cheshire.core :as json]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- customer-sources
  [customer]
  (get-in customer [:sources :data]))

(defn- customer-for-account
  [account-id]
  (:stripe-customer/customer-id
   (one (d/db conn) :stripe-customer/account account-id)))

;; =============================================================================
;; API
;; =============================================================================

;; TODO: Spec a "customer"
(def fetch-customer
  (comp :body service/fetch-customer))

(defn bank-accounts
  [customer]
  (filter #(= "bank_account" (:object %)) (customer-sources customer)))

(defn bank-account
  [customer]
  (first (bank-accounts customer)))

(defn bank-account-verified?
  [customer]
  (= (:status (bank-account customer)) "verified"))

(defn verification-failed?
  [customer]
  (every?
   (fn [{status :status}] (= status "verification_failed"))
   (bank-accounts customer)))

(def bank-account-token
  (comp :id bank-account))

(defn create-customer
  "Create a customer on Stripe with given token and create a corresponding
  entity in our DB."
  [account-id token]
  (let [email (:account/email (d/entity (d/db conn) account-id))
        ;; TODO: Include description w/ building?
        res   (service/create-customer email token)]
    (if-let [error (service/error-from res)]
      ;; Error while creating customer!
      (throw (ex-info "Error encountered while trying to create Stripe customer." error))
      ;; Successful creation of customer
      (let [customer (service/payload-from res)]
        @(d/transact conn [{:db/id                       (tempid)
                            :stripe-customer/customer-id (:id customer)
                            :stripe-customer/account     account-id}])
        ;; return the retrieved customer object
        customer))))

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
  [account-id deposit-1 deposit-2]
  (let [cid      (customer-for-account account-id)
        customer (fetch-customer cid)
        bid      (bank-account-token customer)
        res      (service/verify-source cid bid deposit-1 deposit-2)]
    (if-let [e (service/error-from res)]
      (throw (ex-info "Error encountered while trying to verify source!" e))
      (do
        @(d/transact conn [{:stripe-customer/bank-account-token bid
                            :db/id                              [:stripe-customer/customer-id cid]}])
        (service/payload-from res)))))

(defn create-charge
  "Attempt to create a Stripe charge for given `account-id`. Successful creation
  results in creation of a corresponding `charge`."
  [account-id amount source & {:keys [description customer-id managed-account]}]
  (let [email (:account/email (d/entity (d/db conn) account-id))
        res   (service/charge amount source email
                              :description description
                              :customer-id customer-id
                              :managed-account managed-account)]
    (if-let [e (service/error-from res)]
      (throw (ex-info "Error encountered while trying to create charge!" e))
      (let [payload (service/payload-from res)
            tid     (tempid)
            tx      @(d/transact conn [(assoc-when
                                        {:db/id            tid
                                         :charge/stripe-id (:id payload)
                                         :charge/account   account-id
                                         :charge/status    :charge.status/pending}
                                        :charge/purpose description)])]
        (d/resolve-tempid (d/db conn) (:tempids tx) tid)))))

(s/fdef create-charge
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

;; NOTE: A way to create a unified "message" passing interface would be to
;; create a protocol (e.g. IHasMessage - ha) that could be extended to strings,
;; maps, Exceptions, more?

(defn exception-msg
  [e]
  (-> e ex-data :message))
