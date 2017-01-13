(ns mars.api)

(def ^:private base-uri "/api/v1/mars")

(defn route [& strs]
  (apply str base-uri strs))
