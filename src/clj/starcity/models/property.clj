(ns starcity.models.property
  (:require [starcity.services.stripe :as stripe]
            [starcity.services.stripe.connect :as connect]
            [starcity.datomic :refer [conn tempid]]
            [starcity.models.util :refer :all]
            [datomic.api :as d]
            [starcity.spec]
            [clojure.spec :as s]
            [clj-time.core :as t])
  (:refer-clojure :exclude [name]))

;; =============================================================================
;; API
;; =============================================================================

;; =============================================================================
;; Spec

(s/def :property/name string?)
(s/def :property/available-on :starcity.spec/date)
(s/def :property/cover-image-url string?)
(s/def :property/description string?)

;; =============================================================================
;; Queries

(defn available-units
  "Retrieve all units that are currently available.

  A property is considered *available* iff no :account/unit references it."
  [property]
  (qes '[:find ?u
         :in $ ?p
         :where
         [?p :property/units ?u]
         [?a :account/email _]
         (not [?a :account/unit ?u])]
       (d/db conn) (:db/id property)))

(defn many
  [pattern]
  (->> (find-all-by (d/db conn) :property/name) ; get all properties
       (entids) ; get just their entids
       (d/pull-many (d/db conn) pattern)))

;; =============================================================================
;; Transactions

(defn exists?
  [property-id]
  (:property/name (one (d/db conn) property-id)))

(defn create! [name internal-name units-available]
  (let [entity (ks->nsks :property
                         {:name            name
                          :internal-name   internal-name
                          :units-available units-available})
        tid    (tempid)
        tx     @(d/transact conn [(assoc entity :db/id tid)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))

;; =============================================================================
;; Managed Stripe Accounts

(s/def ::dob :starcity.spec/datetime)

;; TODO: too many params...
;; TODO: Last 4 of ssn?
(defn create-managed-account!
  "Create a new managed account for `property-id`."
  [property-id business-name tax-id account-number routing-number first-name last-name dob]
  (let [res (connect/create-account!
             (connect/owner first-name last-name dob)
             (connect/business business-name tax-id)
             (connect/account account-number routing-number))]
    (if-let [e (stripe/error-from res)]
      (throw (ex-info "Error encountered while trying to create managed account!"
                      {:stripe-error e :property-id property-id}))
      @(d/transact conn [{:db/id                       property-id
                          :property/managed-account-id (:id (stripe/payload-from res))}]))))

(s/fdef create-managed-account!
        :args (s/cat :property-id :starcity.spec/lookup
                     :business-name string?
                     :tax-id string?
                     :account-number string?
                     :routing-number string?
                     :first-name string?
                     :last-name string?
                     :dob ::dob))

;; =============================================================================
;; Selectors

(def name :property/name)
(def internal-name :property/internal-name)
(def managed-account-id :property/managed-account-id)
(def ops-fee :property/ops-fee)

(defn base-rent
  "Determine the base rent at `property` for the given `license`."
  [property license]
  (ffirst
   (d/q '[:find ?price
          :in $ ?property ?license
          :where
          [?property :property/licenses ?plicense]
          [?plicense :property-license/license ?license]
          [?plicense :property-license/base-price ?price]]
        (d/db conn) (:db/id property) (:db/id license))))

(s/fdef base-rent
        :args (s/cat :property :starcity.spec/entity
                     :license :starcity.spec/entity)
        :ret float?)
