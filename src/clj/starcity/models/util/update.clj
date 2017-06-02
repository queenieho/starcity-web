(ns starcity.models.util.update
  (:require [starcity.models.util :refer [one]]
            [starcity.datomic :refer [conn]]
            [clojure.set :as set]
            [datomic.api :as d]
            [toolbelt.predicates :refer [entity?]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- ents->ids
  [entities]
  (set
   (map
    (fn [e]
      (if (entity? e) (:db/id e) e))
    entities)))

;; =============================================================================
;; API
;; =============================================================================

(defn replace-unique
  "Given an entity-id, cardinality many attribute and new values, generate a
  transaction to remove all values that are not present in `new-values' and add
  any values that were not already present."
  [conn entity-id attribute new-values]
  (let [ent        (one (d/db conn) entity-id)
        old-values (set (map :db/id (get ent attribute)))
        to-remove  (set/difference old-values (->> new-values
                                                   (map (comp :db/id (partial d/entity (d/db conn))))
                                                   set))]
    (vec
     (concat
      (map (fn [v] [:db/retract entity-id attribute v]) to-remove)
      (map (fn [v] [:db/add entity-id attribute v]) new-values)))))
