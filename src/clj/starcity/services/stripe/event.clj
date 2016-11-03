(ns starcity.services.stripe.event
  (:require [starcity.services.stripe.request :refer [request]]))

(defn fetch
  [event-id & [cb]]
  (request {:endpoint (format "events/%s" event-id)
            :method   :get}
           {}
           cb))
