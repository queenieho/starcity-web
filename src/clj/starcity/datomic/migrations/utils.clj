(ns starcity.datomic.migrations.utils
  (:require [starcity.models.util :refer [one]]
            [starcity.config :refer [datomic]]
            [datomic.api :as d]))

;; =============================================================================
;; General

(defn add-tempids [txes]
  (map #(assoc % :db/id (d/tempid (:partition datomic))) txes))

(defn tempids [n]
  (repeatedly n (fn [] (d/tempid (:partition datomic)))))

;; =============================================================================
;; Property Seeding

(defn license-id-for-term
  [conn term]
  (:db/id (one (d/db conn) :license/term term)))

(defn property-license
  [conn [term base-price]]
  {:property-license/license    (license-id-for-term conn term)
   :property-license/base-price base-price})
