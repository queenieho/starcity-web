(ns admin.api)

;; TODO: Protocol?

(def ^:private base-uri
  "/api/v1/admin/")

(defn route
  [s]
  (str base-uri s))
