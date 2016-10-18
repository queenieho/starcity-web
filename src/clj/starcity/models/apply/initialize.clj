(ns starcity.models.apply.initialize
  (:require [starcity.models.license :as license]
            [starcity.datomic :refer [conn]]
            [datomic.api :as d]))

(defn- fetch-properties
  "Fetch all properties..."
  []
  (letfn [(-count-available [units]
            (count (filter :unit/available-on units)))]
    (->> (d/q '[:find ?e :where [?e :property/name _]] (d/db conn))
         (map first)
         (d/pull-many (d/db conn) [:property/upcoming
                                   :property/name
                                   :property/internal-name
                                   :property/available-on
                                   {:property/licenses [:property-license/license
                                                        :property-license/base-price]}
                                   {:property/units [:unit/available-on]}])
         (map #(update-in % [:property/units] -count-available)))))

;; TODO: Spec
(defn initial-data
  "Required information to the application client."
  []
  {:properties (fetch-properties)
   :licenses   (license/licenses)})
