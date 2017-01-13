(ns starcity.webhooks.stripe
  (:require [cheshire.core :as json]
            [org.httpkit.client :as http]
            [ring.util.response :as response]
            [starcity.models.stripe.event :as event]
            [starcity.webhooks.stripe.common :refer :all]
            [taoensso.timbre :as t]))

;; =============================================================================
;; Internal
;; =============================================================================

;; An event that we don't have any specific logic to handle.
(defmethod handle-event :default [event-data entity]
  ;; Log it
  (t/trace ::ignore-event event-data)
  ;; Mark it as successful
  (event/succeeded entity))

(defn process [{params :params :as req}]
  ;; First, see if there's an event that exists.
  (if-let [e (event/lookup (event-id params))]
    ;; There is, so we'll only want to process it when it's failed
    (when (event/failed? e)
      (event/processing e)                ; indicate that we're going to work on it
      (handle-event params e))
    ;; There is not, so this is a new event. Create a new event and attempt to
    ;; handle it. NOTE: `handle-event` is responsible for marking the event as
    ;; successful or failed.
    (let [event (event/create (event-id params) (event-type params))
          data  (event/fetch (event-id params))]
      (handle-event data event))))

(comment

  ;; A sample event
  (def event* {:id   "blah"
               :type "charge.succeeded"
               :data {:object {:id     "py_19b42BIvRccmW9nO67ney4Tc"
                               :amount 200000}}})

  (def event* {:id   "blah"
               :type "charge.failed"
               :data {:object {:id              "py_19b42BIvRccmW9nO67ney4Tc"
                               :failure_message "Oh no! It failed!"}}})

  ;; It won't be found on stripe, so mock it in the db
  (let [event (event/create (event-id event*) (event-type event*))]
    ;; Set it as "failed" so that the processing logic picks it up.
    (event/failed event))

  ;; (d/entity (d/db starcity.datomic/conn) [:charge/stripe-id "py_19atOOIvRccmW9nOraTx54U4"])

  (def event* {:id   "evt1"
               :type "customer.source.updated"
               :data {:object {:status   "verification_failed"
                               :customer "cus_9bzpu7sapb8g7y"}}})

  @(http/post "http://localhost:8080/webhooks/stripe"
              {:headers {"Content-Type" "application/json"}
               :body    (json/generate-string event*)})

  )

;; =============================================================================
;; API
;; =============================================================================

(defn hook
  [{params :params :as req}]
  ;; Handle the event in the background
  ;; see https://stripe.com/docs/webhooks#best-practices
  (future (process req))
  ;; Acknowledge that we've received the event.
  (response/response {}))
