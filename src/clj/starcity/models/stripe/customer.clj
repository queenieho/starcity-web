(ns starcity.models.stripe.customer
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [starcity
             [datomic :refer [conn]]
             [util :refer [find-by]]]
            [starcity.datomic.partition :refer [tempid]]
            [starcity.models.account :as account]
            [starcity.services.stripe :as service]
            [starcity.services.stripe.connect :as connect]
            starcity.spec.datomic
            [plumbing.core :refer [assoc-when]]
            [starcity.models.property :as property]
            [starcity.models.member-license :as member-license]))

;; =============================================================================
;; API
;; =============================================================================


(s/def ::id string?)
(s/def ::object #{"customer"})
(s/def ::customer
  (s/or :map (s/keys :req-un [::id ::object])
        :entity :starcity.spec.datomic/entity))

(defprotocol StripeCustomer
  (fetch [stripe-customer]
    "Fetch the Stripe customer associated with this lookup.")
  (id [stripe-customer]
    "Produce the id of the Stripe customer.")
  (active-bank-account [stripe-customer]
    "Fetch the active bank account for this customer.")
  (bank-account-token [stripe-customer]
    "The bank account token of this customer.")
  (has-verified-bank-account? [stripe-customer]
    "Does this customer have a verified bank account?"))

(defn- sources [customer]
  (get-in customer [:sources :data]))

(defn- bank-accounts
  [customer]
  (filter #(= "bank_account" (:object %)) (sources customer)))

(defn- active-bank-account*
  [customer]
  (let [default-source (:default_source customer)
        bank-accounts  (bank-accounts customer)
        default-bank   (find-by (comp #{default-source} :id) bank-accounts)]
    (or default-bank
        (first bank-accounts))))

(defn- bank-account-token*
  "Find either the default bank token for `customer`, or the first one if
  default payment method is not a bank."
  [customer]
  (:id (active-bank-account* customer)))

(s/fdef bank-account-token*
        :args (s/cat :customer ::customer)
        :ret string?)

(defn account-name [bank-account]
  (:bank_name bank-account))

(defn account-last4 [bank-account]
  (:last4 bank-account))

(def has-bank-account?
  "Is there a bank account associated with this `customer`?"
  (comp boolean first bank-accounts))

(s/fdef has-bank-account?
        :args (s/cat :customer ::customer)
        :ret boolean?)

(def ^:private verified-account?
  (comp (partial = "verified") :status))

(defn- has-verified-bank-account?*
  "Does this `customer` have a verified bank account?"
  [customer]
  (boolean
   (find-by verified-account? (bank-accounts customer))))

(s/fdef has-verified-bank-account?*
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

(extend-protocol StripeCustomer

  datomic.query.EntityMap
  (id [stripe-customer]
    (:stripe-customer/customer-id stripe-customer))
  (fetch [stripe-customer]
    (-> stripe-customer id fetch))
  (active-bank-account [stripe-customer]
    (-> stripe-customer id fetch active-bank-account))
  (bank-account-token [stripe-customer]
    (:stripe-customer/bank-account-token stripe-customer))
  (has-verified-bank-account? [stripe-customer]
    (-> stripe-customer id fetch has-verified-bank-account?))

  clojure.lang.IPersistentMap
  (id [customer]
    (:id customer))
  (fetch [customer]
    (fetch (id customer)))
  (active-bank-account [customer]
    (active-bank-account* customer))
  (bank-account-token [customer]
    (bank-account-token* customer))
  (has-verified-bank-account? [customer]
    (has-verified-bank-account?* customer))

  String
  (id [customer-id]
    customer-id)
  (fetch [customer-id]
    (:body (service/fetch-customer customer-id)))
  (active-bank-account [customer-id]
    (active-bank-account (fetch customer-id)))
  (bank-account-token [customer-id]
    (bank-account-token (fetch customer-id)))
  (has-verified-bank-account? [customer-id]
    (has-verified-bank-account? (fetch customer-id))))

;; =============================================================================
;; Actions

(defn- create-stripe-customer
  [customer account & [property]]
  (let [tid (tempid)
        tx  @(d/transact conn [(assoc-when
                                {:db/id                              tid
                                 :stripe-customer/customer-id        (id customer)
                                 :stripe-customer/account            (:db/id account)
                                 :stripe-customer/bank-account-token (bank-account-token customer)}
                                :stripe-customer/managed (:db/id property))])]
    (d/entity (d/db conn) (d/resolve-tempid (d/db conn) (:tempids tx) tid))))

(defn- create-direct!*
  [account property]
  (if-let [platform-customer (account/stripe-customer account)]
    (let [customer   (fetch platform-customer)
          managed    (property/managed-account-id property)
          bank-token (bank-account-token customer)]
      (if bank-token
        (let [{{token :id} :body} (connect/create-token (id platform-customer)
                                                        bank-token
                                                        managed)
              res                 (service/create-customer (account/email account)
                                                           token
                                                           :managed-account managed)]
          (create-stripe-customer (service/payload-from res) account property))
        (throw (ex-info "No bank account exists for customer!"
                        {:account  (account/email account)
                         :property (property/internal-name property)
                         :customer (id platform-customer)}))))
    (throw (ex-info "No platform customer exists; cannot create managed customer!"
                    {:account  (account/email account)
                     :property (property/internal-name property)}))))

(defn create-direct!
  "Create a customer to represent `account` directly on the managed Stripe
  account for `property`. If a customer has already been created, return that
  instead."
  [account property]
  (if-let [customer (member-license/customer account)]
    customer
    (create-direct!* account property)))

(s/fdef create-direct!
        :args (s/cat :account :starcity.spec.datomic/entity
                     :property :starcity.spec.datomic/entity)
        :ret ::customer)

(defn create-platform!
  "Create a customer on Stripe with given token and create a corresponding
  entity in our DB."
  [account token]
  (let [res (service/create-customer (account/email account) token)]
    (if-let [error (service/error-from res)]
      ;; Error while creating customer!
      (throw (ex-info "Error encountered while trying to create Stripe customer." error))
      ;; Successful creation of customer
      (let [customer (service/payload-from res)]
        {:entity   (create-stripe-customer customer account)
         :customer customer}))))

(s/fdef create-platform!
        :args (s/cat :account :starcity.spec.datomic/entity
                     :token string?)
        :ret (s/keys :req-un [::customer :starcity.spec.datomic/entity]))

;; =============================================================================
;; Deletion

(defn delete!
  "Deletes a Stripe customer."
  [stripe-customer]
  (let [res (service/delete-customer (id stripe-customer))]
    (if-let [error (service/error-from res)]
      (throw (ex-info "Error encountered while trying to delete Stripe customer." error))
      @(d/transact conn [[:db.fn/retractEntity (:db/id stripe-customer)]]))))

(s/fdef delete!
        :args (s/cat :account :starcity.spec.datomic/entity))

;; =============================================================================
;; Bank Account Verification

(defn verify-microdeposits
  "Attempt to verify the microdeposit amounts for given `account-id`. Successful
  verification results in the `:stripe-customer/bank-account-token` attribute being
  added to the `:stripe-customer` entity."
  [stripe-customer deposit-1 deposit-2]
  (let [cid      (id stripe-customer)
        customer (fetch cid)
        bid      (bank-account-token customer)
        res      (service/verify-source cid bid deposit-1 deposit-2)]
    (if-let [e (service/error-from res)]
      (throw (ex-info "Error encountered while trying to verify source!" e))
      (do
        @(d/transact conn [{:stripe-customer/bank-account-token bid
                            :db/id                              [:stripe-customer/customer-id cid]}])
        (service/payload-from res)))))
