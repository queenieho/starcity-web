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
                 {:member-application/desired-properties [:db/id]}
                 {:member-application/pet [:db/id :pet/type :pet/weight :pet/breed]}]]
    (d/pull (d/db conn) pattern app-id)))

(defn pull-leases
  "Pull available leases."
  []
  (->> (d/q '[:find ?e :where [?e :available-lease/term _]] (d/db conn))
       (map first)
       (d/pull-many (d/db conn) [:db/id :available-lease/term :available-lease/price])))

(defn pull-properties
  []
  (letfn [(-count-available [units]
            (-> (filter :unit/available-on units)
                (count)))]
    (->> (d/q '[:find ?e :where [?e :property/name _]] (d/db conn))
         (map first)
         (d/pull-many (d/db conn) [:db/id :property/name :property/available-on
                                   {:property/units [:unit/available-on]}])
         (map #(update-in % [:property/units] -count-available)))))

(defn property-by-internal-name
  [internal-name]
  (one (d/db conn) :property/internal-name internal-name))

(comment

  (pull-properties)


  )
