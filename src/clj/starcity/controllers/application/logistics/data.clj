(ns starcity.controllers.application.logistics.data
  (:require [clj-time
             [coerce :as c]
             [format :as f]]
            [clojure.spec :as s]
            [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.datomic.util :refer [one]]
            [starcity.models
             [application :as application]
             [property :as property]]))

;; =============================================================================
;; Helpers
;; =============================================================================

;; =============================================================================
;; Dates

(def ^:private basic-date-formatter (f/formatter :basic-date))

;; =============================================================================
;; API
;; =============================================================================

(defn pull-application
  "Pull the application data from the DB for applicant identified by
  `account-id'."
  [account-id]
  (let [app-id  (:db/id (application/by-account-id account-id))
        pattern [:db/id
                 :rental-application/desired-lease
                 :rental-application/desired-availability
                 {:rental-application/pet [:db/id :pet/type :pet/weight :pet/breed]}]]
    (d/pull (d/db conn) pattern app-id)))

(comment

  (let [account-id (:db/id (one (d/db conn) :account/email "test@test.com"))]
    (pull-application account-id))

  )

(defn pull-available-units
  "Pull all available units for property identified by `property-id'."
  [property-id]
  (let [pattern [:db/id :unit/name :unit/description :unit/price :unit/available-on :unit/floor]]
    (->> (property/available-units property-id)
         (d/pull-many (d/db conn) pattern))))

(defn pull-property
  "Pull property data from the DB for property identified by `property-id'."
  [property-id]
  (let [pattern [:db/id
                 {:property/available-leases [:db/id
                                              :available-lease/price
                                              :available-lease/term]}]]
    (d/pull (d/db conn) pattern property-id)))

(defn property-by-internal-name
  [internal-name]
  (one (d/db conn) :property/internal-name internal-name))

(defn availability
  "Given seq of :basic-date formatted dates, produce a seq of `java.util.Date'
  that are available at the property identified by `property-id'."
  [property-id basic-dates]
  (let [avail-m (->> (property/available-units property-id)
                     (d/pull-many (d/db conn) [:unit/available-on])
                     (reduce (fn [acc {date :unit/available-on}]
                               (let [fmt (f/unparse basic-date-formatter (c/from-date date))]
                                 (assoc acc fmt date)))
                             {}))]
    (map (partial get avail-m) basic-dates)))

(s/fdef availability
        :args (s/cat :property-id int?
                     :basic-dates (s/spec (s/+ :starcity.spec/basic-date)))
        :ret (s/* :starcity.spec/date))
