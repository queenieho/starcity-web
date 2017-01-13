(ns starcity.webhooks.stripe.common
  (:require [clojure.core.async :refer [go <!]]
            [starcity.models.stripe.event :as event]))

(def event-id :id)
(def event-type :type)

(defmulti handle-event
  "Stripe event handler."
  (fn [event-data _] (event-type event-data)))

(defn manage
  "Asynchronously takes from channel `c` and inspects the resulting value; if
  the value is a Throwable, marks the event as failed."
  [event c]
  (go (let [v (<! c)]
        (if (instance? Throwable v)
          (event/failed event)
          (event/succeeded event)))))
