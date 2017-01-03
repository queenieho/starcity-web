(ns starcity.models.apply.initialize
  (:require [starcity.models.license :as license]
            [datomic.api :as d]
            [starcity.models.property :as property]))

(defn- properties [conn]
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

(defn- fetch-properties [conn]
  (->> (properties conn)
       (d/pull-many (d/db conn) property-pattern)
       (map #(assoc-in % [:property/units] (count (property/available-units %))))))

;; TODO: Spec
(defn initial-data
  "Required information to the application client."
  [conn]
  (letfn [(clientize [l]
            {:license/term (:license/term l)
             :db/id        (:db/id l)})]
    {:properties (fetch-properties conn)
     :licenses   (->> (license/licenses conn)
                      (map clientize))}))
