(ns starcity.models.apply.initialize
  (:require [starcity.models.license :as license]
            [starcity.datomic :refer [conn]]
            [datomic.api :as d]
            [starcity.models.property :as property]))

(defn- properties []
  (d/q '[:find [?e ...] :where [?e :property/name _]] (d/db conn)))

(def property-pattern
  [:db/id
   :property/upcoming
   :property/name
   :property/internal-name
   :property/available-on
   {:property/licenses [:property-license/license
                        :property-license/base-price]}])

;; TODO: Don't use pull api, use a parse fn

(defn- fetch-properties []
  (->> (properties)
       (d/pull-many (d/db conn) property-pattern)
       (map #(assoc-in % [:property/units] (count (property/available-units %))))))

;; TODO: Spec
(defn initial-data
  "Required information to the application client."
  []
  {:properties (fetch-properties)
   :licenses   (license/licenses)})
