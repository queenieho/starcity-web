(ns starcity.webhooks.stripe
  (:require [blueprints.models.events :as events]
            [cheshire.core :as json]
            [datomic.api :as d]
            [org.httpkit.client :as http]
            [ring.util.response :as response]
            [starcity.config :as config :refer [config]]
            [starcity.datomic :refer [conn]]))


(defn hook
  [{params :params :as req}]
  (let [{:keys [id type livemode account]} params]
    (when-not (some? (d/entity (d/db conn) [:event/id id]))
      ;; If we're in production...
      (if (config/is-production? config)
        ;; Only accept events that are sent in `livemode` in production
        (when livemode
          @(d/transact-async conn [(events/stripe-event id type account)]))
        ;; Accept all events during development
        @(d/transact-async conn [(events/stripe-event id type account)])))
    ;; Acknowledge that we've received the event.
    (response/response {})))


(comment

  ;; A sample event
  (do
    (def event* {:id      "evt_19hS9QJDow24Tc1atld0vATs"
                 :type    "customer.subscription.trial_will_end"
                 :account "acct_191838JDow24Tc1a"})

    @(http/post "http://localhost:8080/webhooks/stripe"
                {:headers {"Content-Type" "application/json"}
                 :body    (json/generate-string event*)}))

  )
