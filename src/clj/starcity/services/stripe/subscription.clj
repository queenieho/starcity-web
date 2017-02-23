(ns starcity.services.stripe.subscription
  (:require [clojure.spec :as s]
            [starcity.services.stripe.request :refer [request]]
            [plumbing.core :refer [assoc-when]]))

(def ^:private endpoint "subscriptions")

;; =============================================================================
;; Create!

(defn create!
  [customer-id plan-id & {:keys [source managed fee-percent trial-end]}]
  (when fee-percent
    (assert managed "When a `fee-percent` is specified, `managed` must also be supplied."))
  (request (assoc-when
            {:endpoint endpoint
             :method   :post}
            :managed-account managed)
           (assoc-when
            {:customer customer-id
             :plan     plan-id}
            :source source
            :application_fee_percent fee-percent
            :trial_end trial-end)))

(s/def ::id string?)
(s/def ::source string?)
(s/def ::managed string?)
(s/def ::fee-percent float?)
(s/def ::trial-end integer?)

(s/fdef create!
        :args (s/cat :customer-id string?
                     :plan-id string?
                     :opts (s/keys* :opt-un [::source
                                             ::managed
                                             ::fee-percent
                                             ::trial-end]))
        :ret (s/keys :req-un [::id]))

;; =============================================================================
;; Update!

(defn update!
  [subscription-id & {:keys [managed fee-percent]}]
  (request (assoc-when
            {:endpoint (str endpoint "/" subscription-id)
             :method   :post}
            :managed-account managed)
           (assoc-when
            {}
            :application_fee_percent fee-percent)))

(s/fdef update!
        :args (s/cat :subscription ::id
                     :opts (s/keys* :opt-un [::managed ::fee-percent])))

;; =============================================================================
;; Fetch

(defn fetch
  [id & {:keys [managed]}]
  (request (assoc-when
            {:endpoint (str endpoint "/" id)
             :method  :get}
            :managed-account managed)
           {}))

(s/fdef fetch
        :args (s/cat :id ::id
                     :opts (s/keys* :opt-un [::managed])))
