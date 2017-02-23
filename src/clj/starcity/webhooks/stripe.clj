(ns starcity.webhooks.stripe
  (:require [cheshire.core :as json]
            [datomic.api :as d]
            [org.httpkit.client :as http]
            [ring.util.response :as response]
            [starcity
             [datomic :refer [conn]]
             [environment :refer [is-production?]]]
            [starcity.models.cmd :as cmd]))

;; =============================================================================
;; API
;; =============================================================================

(defn hook
  [{params :params :as req}]
  (let [{:keys [id type livemode user_id]} params]
    (when-not (d/entity (d/db conn) [:cmd/id id])
      ;; If we're in production...
      (if (is-production?)
        ;; Only accept events that are sent in `livemode`
        (when livemode
          @(d/transact conn [(cmd/stripe-webhook-event id type user_id)]))
        ;; If it's development, accept all
        @(d/transact conn [(cmd/stripe-webhook-event id type user_id)])))
    ;; Acknowledge that we've received the event.
    (response/response {})))

(comment

  ;; A sample event
  (do
    (def event* {:id      "evt_19hS9QJDow24Tc1atld0vATs"
                 :type    "customer.subscription.trial_will_end"
                 :user_id "acct_191838JDow24Tc1a"})

    @(http/post "http://localhost:8080/webhooks/stripe"
                {:headers {"Content-Type" "application/json"}
                 :body    (json/generate-string event*)}))

  (d/touch (d/entity (d/db conn) [:cmd/id (:id event*)]))

  @(d/transact conn [(cmd/retry (d/entity (d/db conn) [:cmd/id (:id event*)]))])

  )
