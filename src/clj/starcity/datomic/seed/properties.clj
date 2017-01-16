(ns starcity.datomic.seed.properties
  (:require [starcity.datomic.partition :refer [tempid]]
            [plumbing.core :refer [assoc-when]]
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

(defn units [property-name n]
  (for [i (range n)]
    {:unit/name (format "%s %s" property-name (inc i))}))

(defn property
  [name internal-name available-on address licenses units
   & {:keys [managed-account-id ops-fee]}]
  (assoc-when
   {:db/id                  (tempid)
    :property/name          name
    :property/internal-name internal-name
    :property/available-on  available-on
    :property/licenses      licenses
    :property/units         units}
   :property/managed-account-id managed-account-id
   :property/ops-fee ops-fee))

(defn seed [conn]
  (let [licenses (partial licenses conn)]
    @(d/transact conn [(property "West SoMa"
                                 "52gilbert"
                                 #inst "2016-12-01T00:00:00.000-00:00"
                                 (address "52 Gilbert St.")
                                 (licenses [1 2300.0] [3 2300.0] [6 2100.0] [12 2000.0])
                                 (units "52gilbert" 6)
                                 :managed-account-id "acct_191838JDow24Tc1a"
                                 :ops-fee 28.0)
                       (property "The Mission"
                                 "2072mission"
                                 #inst "2017-01-01T00:00:00.000-00:00"
                                 (address "2072 Mission St.")
                                 (licenses [1 2400.0] [6 2200.0] [12 2100.0])
                                 (units "2072mission" 17))])))
