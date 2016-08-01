(ns starcity.controllers.application.logistics.data
  (:require [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.models.util :refer [one qes find-all-by entids]]
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
                 :member-application/desired-license
                 :member-application/desired-availability
                 {:member-application/desired-properties [:db/id]}
                 {:member-application/pet [:db/id :pet/type :pet/weight :pet/breed]}]]
    (d/pull (d/db conn) pattern app-id)))

(defn pull-licenses
  "Pull available licenses."
  []
  (->> (find-all-by (d/db conn) :license/term)
       (entids)
       (d/pull-many (d/db conn) [:db/id :license/term])))

(defn pull-properties
  []
  (letfn [(-count-available [units]
            (count (filter :unit/available-on units)))]
    (->> (d/q '[:find ?e :where [?e :property/name _]] (d/db conn))
         (map first)
         (d/pull-many (d/db conn) [:db/id
                                   :property/upcoming
                                   :property/name
                                   :property/available-on
                                   {:property/licenses [:property-license/license
                                                        :property-license/base-price]}
                                   {:property/units [:unit/available-on]}])
         (map #(update-in % [:property/units] -count-available)))))

(defn property-by-internal-name
  [internal-name]
  (one (d/db conn) :property/internal-name internal-name))
