(ns starcity.models.property
  (:require [starcity.services.stripe :as stripe]
            [starcity.services.stripe.connect :as connect]
            [starcity.datomic :refer [conn tempid]]
            [starcity.models.util :refer :all]
            [datomic.api :as d]
            [starcity.spec]
            [clojure.spec :as s]
            [clj-time.core :as t]))

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

  A property is considered *available* iff it has a value under
  its :unit/available-on attribute."
  [property-id]
  (->> (d/q '[:find ?units
              :in $ ?property
              :where
              [?property :property/units ?units]
              [?units :unit/available-on _]]
            (d/db conn) property-id)
       (map first)))

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

(comment

  (create-managed-account! [:property/internal-name "52gilbert"]
                           "52 Gilbert LLC"
                           "61-1799315"
                           "000111111116"
                           "110000000")

  (:property/managed-account-id (d/entity (d/db conn) [:property/internal-name "52gilbert"]))

  )
