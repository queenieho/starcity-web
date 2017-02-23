(ns starcity.models.apply.initialize
  (:require [starcity.models.license :as license]
            [datomic.api :as d]
            [starcity.models.property :as property]))

(defn- properties [conn]
  (d/q '[:find [?e ...] :where [?e :property/name _]] (d/db conn)))

(defn- clientize-license [license]
  (-> (select-keys license [:property-license/license
                            :property-license/base-price
                            :db/id])
      (update :property-license/license select-keys [:db/id :license/term])))

(defn- clientize-property [conn id]
  (let [property (d/entity (d/db conn) id)]
    {:db/id                  (:db/id property)
     :property/name          (:property/name property)
     :property/internal-name (:property/internal-name property)
     :property/available-on  (:property/available-on property)
     :property/licenses      (map clientize-license (:property/licenses property))
     :property/units         (count (property/available-units conn property))}))

(defn initial-data
  "Required information to the application client."
  [conn]
  (letfn [(clientize [l]
            {:license/term (:license/term l)
             :db/id        (:db/id l)})]
    {:properties (map (partial clientize-property conn) (properties conn))
     :licenses   (->> (license/licenses conn)
                      (map clientize))}))
