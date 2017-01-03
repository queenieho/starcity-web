(ns starcity.datomic.seed.properties
  (:require [starcity.datomic.partition :refer [tempid]]
            [datomic.api :as d]))

(defn license [conn term]
  (d/q '[:find ?e .
         :in $ ?term
         :where [?e :license/term ?term]]
       (d/db conn) term))

(defn licenses [conn & ls]
  (map
   (fn [[term price]]
     {:property-license/license    (license conn term)
      :property-license/base-price price})
   ls))

(defn address [lines]
  {:address/lines lines
   :address/city  "San Francisco"})

(defn units [n]
  (for [i (range n)]
    {:unit/name (str "Unit " i)}))

(defn property
  [name internal-name available-on address licenses units]
  {:db/id                  (tempid)
   :property/name          name
   :property/internal-name internal-name
   :property/available-on  available-on
   :property/licenses      licenses
   :property/units         units})

(defn seed [conn]
  (let [licenses (partial licenses conn)]
    @(d/transact conn [(property "West SoMa"
                                 "52gilbert"
                                 #inst "2016-12-01T00:00:00.000-00:00"
                                 (address "52 Gilbert St.")
                                 (licenses [1 2300.0] [3 2300.0] [6 2100.0] [12 2000.0])
                                 (units 6))
                       (property "The Mission"
                                 "2072mission"
                                 #inst "2017-01-01T00:00:00.000-00:00"
                                 (address "2072 Mission St.")
                                 (licenses [1 2400.0] [6 2200.0] [12 2100.0])
                                 (units 16))])))
