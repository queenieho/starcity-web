(ns starcity.datomic.transaction
  (:require [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.datomic.util :refer :all]
            [clojure.set :as set]))

(defn map-form->list-form
  [entity-id m]
  (reduce (fn [acc [k v]]
            (conj acc [:db/add entity-id k v]))
          [] m))

(defn replace-unique
  "Given an entity-id, cardinality many attribute and new values, generate a
  transact to remove all values that are not present in `new-values' and add
  any values that were not already present."
  [entity-id attribute new-values]
  (let [ent        (one (d/db conn) entity-id)
        old-values (get ent attribute)
        to-remove  (set/difference old-values (set new-values))]
    (vec
     (concat
      (map (fn [v] [:db/retract entity-id attribute v]) to-remove)
      (map (fn [v] [:db/add entity-id attribute v]) new-values)))))
