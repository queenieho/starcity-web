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

(defn keys-for-approval
  "Given an account-id, find the Stripe public and private keys for the property
  that this account is approved for. Throws an exception when none are found."
  [account-id]
  (let [approval    (one (d/db conn) :approval/account account-id)
        -lookup-key #(get-in approval [:approval/property %])
        secret-key  (-lookup-key :property/stripe-secret-key)
        public-key  (-lookup-key :property/stripe-public-key)
        data        {:account-id account-id}]
    (cond
      (nil? approval)   (throw (ex-info "No approval for this account!" data))
      (nil? secret-key) (throw (ex-info "No secret key for this account!" data))
      (nil? public-key) (throw (ex-info "No public key for this account!" data))
      :otherwise        [public-key secret-key])))

(s/fdef keys-for-approval
        :args (s/cat :account-id :starcity.spec/lookup)
        :ret  (s/cat :public-key string?
                     :secret-key string?))

;; TODO: Spec a "customer"
(def fetch-customer
  (comp :body service/fetch-customer))

(defn bank-account
  [customer]
  (first
   (filter #(= "bank_account" (:object %)) (customer-sources customer))))

(defn bank-account-verified?
  [customer]
  (= (:status (bank-account customer)) "verified"))

(def bank-account-token
  (comp :id bank-account))

(defn create-customer
  "Create a customer on Stripe with given token and create a corresponding
  entity in our DB."
  [account-id token]
  (let [email          (:account/email (d/entity (d/db conn) account-id))
        [_ secret-key] (keys-for-approval account-id)
        ;; TODO: Include description w/ building?
        res            (service/create-customer secret-key email token)]
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

(defn verify-microdeposits
  "Attempt to verify the microdeposit amounts for given `account-id`. Successful
  verification results in the `:stripe-customer/bank-account-token` attribute being
  added to the `:stripe-customer` entity."
  [account-id deposit-1 deposit-2]
  (let [cid            (customer-for-account account-id)
        [_ secret-key] (keys-for-approval account-id)
        customer       (fetch-customer secret-key cid)
        bid            (bank-account-token customer)
        res            (service/verify-source secret-key cid bid deposit-1 deposit-2)]
    (if-let [e (service/error-from res)]
      (throw (ex-info "Error encountered while trying to verify source!" e))
      (do
        @(d/transact conn [{:stripe-customer/bank-account-token bid
                            :db/id                              [:stripe-customer/customer-id cid]}])
        (service/payload-from res)))))

(defn create-charge
  "Attempt to create a Stripe charge for given `account-id`. Successful creation
  results in creation of a corresponding `charge`."
  [account-id amount source & {:keys [description customer-id]}]
  (let [email          (:account/email (d/entity (d/db conn) account-id))
        [_ secret-key] (keys-for-approval account-id)
        res            (service/charge amount source email
                                       :description description
                                       :customer-id customer-id
                                       :secret-key secret-key)]
    (if-let [e (service/error-from res)]
      (throw (ex-info "Error encountered while trying to create charge!" e))
      (let [payload (service/payload-from res)
            tid     (tempid)
            tx      @(d/transact conn [(assoc-when
                                        {:db/id            tid
                                         :charge/stripe-id (:id payload)
                                         :charge/account   account-id}
                                        :charge/purpose description)])]
        (d/resolve-tempid (d/db conn) (:tempids tx) tid)))))

(s/fdef create-charge
        :args (s/cat :account-id :starcity.spec/lookup
                     :amount pos-int?
                     :source string?
                     :opts (s/keys* :opt-un [::customer-id ::description]))
        :ret integer?)

;; =============================================================================
;; Misc.

;; NOTE: A way to create a unified "message" passing interface would be to
;; create a protocol (e.g. IHasMessage - ha) that could be extended to strings,
;; maps, Exceptions, more?

(defn exception-msg
  [e]
  (-> e ex-data :message))


(comment

  (def sample-customer (json/parse-string (slurp (io/resource "sample-customer.json")) true))

  (bank-account-verified? "cus_96V5gpDRHp4BP9")

  (verify-microdeposits [:account/email "onboarding@test.com"] 1 2)

  ;; (:account/email (d/entity (d/db conn) 285873023222776))

  (s/valid? (s/cat :account-id :starcity.spec/lookup
                   :amount pos-int?
                   :source string?
                   :opts (s/keys* :opt-un [::customer-id ::description]))
            [12345 50000 "abcd" :description "hello"])

  )
