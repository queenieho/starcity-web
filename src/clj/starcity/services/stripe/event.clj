(ns starcity.services.stripe.event
  (:require [plumbing.core :refer [assoc-when]]
            [starcity.services.stripe.request :refer [request]]))

(defn fetch
  [event-id & {:keys [managed cb]}]
  (request (assoc-when
            {:endpoint (format "events/%s" event-id)
             :method   :get}
            :managed-account managed)
           {}
           cb))

(comment

  (fetch "evt_19hS9QJDow24Tc1atld0vATs"
         :managed "acct_191838JDow24Tc1a")

  )
