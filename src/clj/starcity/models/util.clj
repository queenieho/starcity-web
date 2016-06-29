(ns starcity.models.util
  (:require [starcity.datomic.util :refer [one]]
            [starcity.datomic :refer [conn]]
            [datomic.api :as d]))

;; =============================================================================
;; API

(def Entity datomic.query.EntityMap)

(defn ks->nsks [ns m]
  (let [ns (if (string? ns) ns (name ns))]
    (reduce (fn [acc [k v]]
              (if (namespace k)
                (assoc acc k v)         ; don't overwrite existing namespace
                (assoc acc (keyword ns (name k)) v)))
            {}
            m)))

(defn- gen-tx
  "TODO: documentation"
  [params txfns]
  (->> (keys params)
       (reduce (fn [fns k]
                 (if (contains? params k)
                   (conj fns (get txfns k))
                   fns))
               [])
       (apply juxt)))

(defn make-update-fn
  [txfns]
  (fn [entity-id params]
    (let [tx (->> ((gen-tx params txfns) (one (d/db conn) entity-id) params)
                  (apply concat))]
      @(d/transact conn (vec tx))
      entity-id) ))
