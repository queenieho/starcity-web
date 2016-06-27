(ns starcity.datomic.util
  (:require [datomic.api :as d]))

(defn ident
  [conn id]
  (:db/ident (d/entity (d/db conn) id)))

(defn only
  "Return the only item from a query result"
  [query-result]
  (assert (= 1 (count query-result)))
  (assert (= 1 (count (first query-result))))
  (ffirst query-result))

;; (defprotocol Eid
;;   (e [_]))

;; (extend-protocol Eid
;;   java.lang.Long
;;   (e [n] n)

;;   datomic.Entity
;;   (e [ent] (:db/id ent)))

(defn qe
  "Returns the single entity returned by a query."
  [query db & args]
  (let [res (apply d/q query db args)]
    (d/entity db (only res))))

(defn qes
  "Returns the entities returned by a query, assuming that
   all :find results are entity ids."
  [query db & args]
  (->> (apply d/q query db args)
       (mapv (fn [items]
               (mapv (partial d/entity db) items)))
       (apply concat)))

(defn qe1
  "Returns the first entity returned by a query."
  [query db & args]
  (->> (apply d/q query db args)
       (mapv (fn [items]
               (mapv (partial d/entity db) items)))
       (apply concat)
       (first)))

(defn entity-if-exists
  [db id]
  (qe1 '[:find ?eid :in $ ?eid :where [?eid]] db id))

(defn find-by
  "Returns the unique entity identified by attr and val."
  [db attr val]
  (qe1 '[:find ?e
         :in $ ?attr ?val
         :where [?e ?attr ?val]]
       db attr val))

(defn find-all-by
  "Returns all entities possessing attr."
  ([db attr]
   (qes '[:find ?e
          :in $ ?attr
          :where [?e ?attr]]
        db attr))
  ([db attr val]
   (qes '[:find ?e
          :in $ ?attr ?val
          :where [?e ?attr ?val]]
        db attr val)))

(defn one
  "Looks up a record by id or attribute val."
  ([db id]
   (d/entity db id))
  ([db attr v]
   (find-by db attr v)))

(defn qfs
  "Returns the first of each query result."
  [query db & args]
  (->> (apply d/q query db args)
       (mapv first)))

(defn modes
  "Returns the set of modes."
  [coll]
  (->> (frequencies coll)
       (reduce
        (fn [[modes ct] [k v]]
          (cond
            (< v ct) [modes ct]
            (= v ct) [(conj modes k) ct]
            (> v ct) [#{k} v]))
        [#{} 2])
       first))

(defn map-form->list-form
  [entity-id m]
  (reduce (fn [acc [k v]]
            (conj acc [:db/add entity-id k v]))
          [] m))
