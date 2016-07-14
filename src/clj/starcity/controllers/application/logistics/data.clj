(ns starcity.controllers.application.logistics.data
  (:require [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.datomic.util :refer [one]]
            [starcity.models.application :as application]))

;; =============================================================================
;; API
;; =============================================================================

(defn pull-application
  "Pull the application data from the DB for applicant identified by
  `account-id'."
  [account-id]
  (let [app-id  (:db/id (application/by-account-id account-id))
        pattern [:db/id
                 :member-application/desired-lease
                 :member-application/desired-availability
                 {:member-application/pet [:db/id :pet/type :pet/weight :pet/breed]}]]
    (d/pull (d/db conn) pattern app-id)))

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
