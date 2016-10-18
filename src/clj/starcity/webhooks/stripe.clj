(ns starcity.webhooks.stripe)

;; =============================================================================
;; API
;; =============================================================================

(defn hook
  [{:keys [params] :as req}]
  (clojure.pprint/pprint req)
  (let [event-type (:type params)]
    ;; TODO: Endpoint should respond immediately and perform rest async
    (condp = event-type
      "customer.source.updated" {} ;; TODO: do stuff
      {:status 200}                ; unrecognized, respond ok
      )))
