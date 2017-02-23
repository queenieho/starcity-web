(ns starcity.services.stripe.customer
  (:require [starcity.services.stripe.request :as req :refer [request]]
            [clojure.spec :as s]))

(defn- delete!* [customer-id]
  (request {:endpoint (format "customers/%s" customer-id)
            :method   :delete}
           {}))

(defn delete!
  "Delete a Stripe customer."
  [customer-id]
  (let [res (delete!* customer-id)]
    (if-let [error (req/error-from res)]
      (throw (ex-info "Error encountered while trying to delete Stripe customer." error))
      (req/payload-from res))))

(s/fdef delete!
        :args (s/cat :customer-id string?)
        :ret map?)
