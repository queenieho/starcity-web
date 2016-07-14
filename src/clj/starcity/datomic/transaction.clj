(ns starcity.datomic.transaction
  (:require [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.datomic.util :refer :all]
            [clojure.set :as set]))

(defn- ents->ids
  [entities]
  (set
   (map
    (fn [e]
      (if (entity? e) (:db/id e) e))
    entities)))

(defn replace-unique
  "Given an entity-id, cardinality many attribute and new values, generate a
  transact to remove all values that are not present in `new-values' and add
  any values that were not already present."
  [entity-id attribute new-values]
  (let [ent        (one (d/db conn) entity-id)
        old-values (ents->ids (get ent attribute))
        to-remove  (set/difference old-values (set new-values))]
    (vec
     (concat
      (map (fn [v] [:db/retract entity-id attribute v]) to-remove)
      (map (fn [v] [:db/add entity-id attribute v]) new-values)))))
