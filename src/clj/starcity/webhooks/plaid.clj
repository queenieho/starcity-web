(ns starcity.webhooks.plaid
  (:require [starcity.api.common :refer [ok?]]
            [starcity.models.plaid :as model]
            [starcity.services.plaid :as service]
            [taoensso.timbre :as timbre]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private INCOME-SUCCESS 10)

(defn- log-failure
  ([account-id params]
   (log-failure "Plaid failure" account-id params))
  ([message account-id {:keys [status body] :as res}]
   (timbre/warnf "PLAID: %s :: account-id - %s :: status - %s :: code - %s :: plaid message - %s :: resolve - %s"
                 message account-id status (:code body) (:message body) (:resolve body))))

;; =============================================================================
;; API
;; =============================================================================

(defn hook
  "Handler for Plaid webhooks. This will be triggered some time (up to a few
  minutes) after the upgrade request has been sent, as Plaid needs to pull the
  transaction log and process it."
  [{:keys [params] :as req}]
  (letfn [(-on-income [{:keys [body] :as res}]
            (let [account-id (:db/id identity)]
              (if (ok? res)
                (model/add-income-data! (:access_token body) params)
                (log-failure "Error while retrieving income information!" account-id res))))]
    (let [{:keys [code message access_token]} params]
      (timbre/info "PLAID WEBHOOK: Received request!" params)
      (condp = code
        INCOME-SUCCESS (service/get-income access_token -on-income)
        (timbre/warnf "PLAID WEBHOOK: Unrecognized status code :: code - %s :: message - %s :: account-id - %s"
                      code message (:db/id identity))))))
