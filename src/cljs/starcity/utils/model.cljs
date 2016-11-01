(ns starcity.utils.model)

(defn assoc-in-db*
  "Creates functions like `clojure.core/assoc-in` that prepend a `root-db-key`
  to the beginning of the path (`ks`)."
  [root-db-key]
  (fn [db ks v]
    (assoc-in db (concat [root-db-key] ks) v)))

(defn get-in-db*
  "Creates functions like `clojure.core/get-in` that prepend s `root-db-key` to
  the beginning of the lookup if `db` has that key -- otherwise behaves exactly
  like `clojure.core/get-in`"
  [root-db-key]
  (fn [db ks]
    (if-let [db' (get db root-db-key)]
      ;; We're searching from the root
      (get-in db' ks)
      ;; We're already under `root-db-key`
      (get-in db ks))))
