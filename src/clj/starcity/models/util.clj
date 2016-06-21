(ns starcity.models.util)

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
