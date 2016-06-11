(ns starcity.router)

(defmulti route
  "Route a web request."
  (fn [match req]
    (when-let [handler (:handler match)]
      [handler (:request-method req)])))

;; TODO: log unmatched request?
(defmethod route nil [_ req]
  req)
