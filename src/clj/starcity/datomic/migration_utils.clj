(ns starcity.datomic.migration-utils
  (:require [datomic.api :as d]
            [starcity.models.util :refer [one]]))

;; =============================================================================
;; General

(defn add-tempids [txes]
  (map #(assoc % :db/id (d/tempid :db.part/starcity)) txes))

;; =============================================================================
;; Property Seeding

(defn license-id-for-term
  [conn term]
  (:db/id (one (d/db conn) :license/term term)))

(defn property-license
  [conn [term base-price]]
  {:property-license/license    (license-id-for-term conn term)
   :property-license/base-price base-price})
