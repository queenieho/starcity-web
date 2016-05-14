(ns starcity.models.util)

;; =============================================================================
;; API

(def Entity datomic.query.EntityMap)

(defn mapify [ns m]
  (let [ns (if (string? ns) ns (name ns))]
    (reduce (fn [acc [k v]]
              (assoc acc (keyword ns (name k)) v))
            {}
            m)))
