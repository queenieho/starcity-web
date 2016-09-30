(ns apply.api)

(def ^:private base-uri
  "/api/v1/apply/")

(defn route [& fragments]
  (->> fragments (interpose "/") (apply str base-uri)))
