(ns starcity.router)

(defmulti route
  "Route a web request."
  (fn [match _]
    (when-let [handler (:handler match)]
      [handler (:request-method handler)])))

;; TODO: log unmatched request?
(defmethod route nil [_ req]
  req)
