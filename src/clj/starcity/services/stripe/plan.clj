(ns starcity.services.stripe.plan
  (:require [clojure.spec :as s]
            [starcity.services.stripe.request :refer [request]]
            [plumbing.core :refer [assoc-when]]))

(def ^:private endpoint "plans")

(def ^:private max-descriptor-length 22)

(defn create!
  "Create a plan to be used with subscriptions."
  [id plan-name amount interval
   & {:keys [trial-days statement-descriptor metadata managed]}]
  (when statement-descriptor
    (assert (<= (count statement-descriptor) max-descriptor-length)
            "The statement descriptor must be less than or equal to 22 characters."))
  (request (assoc-when
            {:endpoint endpoint
             :method   :post}
            :managed-account managed)
           (assoc-when
            {:id       id
             :amount   amount
             :currency "usd"
             :interval (name interval)
             :name     plan-name}
            :trial_period_days trial-days
            :statement_descriptor statement-descriptor)))

(s/def ::interval #{:day :week :month :year})
(s/def ::id string?)
(s/def ::trial-days integer?)
(s/def ::statement-descriptor (s/and string? #(<= (count %) max-descriptor-length)))
(s/def ::metadata map?)
(s/def ::managed string?)

(s/fdef create!
        :args (s/cat :id string?
                     :plan-name string?
                     :amount integer?
                     :inerval ::interval
                     :opts (s/keys* :opt-un [::trial-days
                                             ::statement-descriptor
                                             ::metadata
                                             ::managed]))
        :ret (s/keys :req-un [::id]))

(defn fetch
  [id & {:keys [managed]}]
  (request (assoc-when
            {:endpoint (str endpoint "/" id)
             :method  :get}
            :managed-account managed)
           {}))
