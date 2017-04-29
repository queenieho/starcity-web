(ns starcity.services.stripe.sources
  (:require [clojure.spec :as s]
            [starcity.services.stripe.request :as req :refer [request]]))

(defn create!*
  [customer-id params]
  (request {:endpoint (format "customers/%s/sources" customer-id)
            :method   :post}
           params))

(defn create!
  "Create a new source for the customer identified by `customer-id`."
  [customer-id source]
  (let [res (create!* customer-id {:source source})]
    (if-let [error (req/error-from res)]
      (throw (ex-info "Error encountered while trying to create source for customer." error))
      (req/payload-from res))))

(s/def ::source string?)
(s/fdef update!
        :args (s/cat :customer-id string?
                     :source string?)
        :ret map?)
