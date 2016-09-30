(ns starcity.models.property
  (:require [starcity.datomic :refer [conn]]
            [starcity.config :as config]
            [starcity.models.util :refer :all]
            [datomic.api :as d]
            [starcity.config :refer [datomic] :rename {datomic config}]
            [starcity.spec]
            [clojure.spec :as s]))

;; =============================================================================
;; API
;; =============================================================================

;; =============================================================================
;; Spec

(s/def :property/name string?)
(s/def :property/available-on :starcity.spec/date)
(s/def :property/cover-image-url string?)
(s/def :property/description string?)

;; =============================================================================
;; Queries

(defn available-units
  "Retrieve all units that are currently available.

  A property is considered *available* iff it has a value under
  its :unit/available-on attribute."
  [property-id]
  (->> (d/q '[:find ?units
              :in $ ?property
              :where
              [?property :property/units ?units]
              [?units :unit/available-on _]]
            (d/db conn) property-id)
       (map first)))

(defn many
  [pattern]
  (->> (find-all-by (d/db conn) :property/name) ; get all properties
       (entids) ; get just their entids
       (d/pull-many (d/db conn) pattern)))

;; =============================================================================
;; Transactions

(defn exists?
  [property-id]
  (:property/name (one (d/db conn) property-id)))

(defn create! [name internal-name units-available]
  (let [entity (ks->nsks :property
                         {:name            name
                          :internal-name   internal-name
                          :units-available units-available})
        tid    (d/tempid (:partition config))
        tx     @(d/transact conn [(assoc entity :db/id tid)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))
