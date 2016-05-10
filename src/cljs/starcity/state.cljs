(ns starcity.state)

(defn get-path
  [paths & ks]
  (get-in paths (vec ks)))
