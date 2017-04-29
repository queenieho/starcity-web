(ns starcity.services.stripe.customer
  (:refer-clojure :exclude [update])
  (:require [starcity.services.stripe.request :as req :refer [request]]
            [plumbing.core :as plumbing]
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

(comment
  (defn update!*
    [customer-id params]
    (request {:endpoint (format "customers/%s" customer-id)
              :method   :post}
             params))

  (defn update!
    "Update a customer."
    [customer-id & {:keys [source] :as opts}]
    (assert (not (empty? opts)) "At least one field to update must be supplied!")
    (let [res (update!* customer-id (plumbing/assoc-when {} :default_source source))]
      (if-let [error (req/error-from res)]
        (throw (ex-info "Error encountered while trying to update Stripe customer." error))
        (req/payload-from res))))

  (s/def ::source string?)
  (s/fdef update!
          :args (s/cat :customer-id string?
                       :opts (s/keys* :opt-un [::source]))
          :ret map?))
