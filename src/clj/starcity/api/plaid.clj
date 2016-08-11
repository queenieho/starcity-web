(ns starcity.api.plaid
  (:require [starcity.services.plaid :as service]
            [starcity.models.plaid :as model]
            [starcity.environment :refer [environment]]
            [starcity.api.common :refer [ok malformed ok?]]
            [taoensso.timbre :as timbre]
            [clojure.spec :as s]
            [ring.util.response :as response]))

(timbre/refer-timbre)

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private INCOME-SUCCESS 10)

(defn- log-failure
  ([account-id params]
   (log-failure "Plaid failure" account-id params))
  ([message account-id {:keys [status body] :as res}]
   (warnf "PLAID: %s :: account-id - %s :: status - %s :: code - %s :: plaid message - %s :: resolve - %s"
          message account-id status (:code body) (:message body) (:resolve body))))

;; =============================================================================
;; API
;; =============================================================================

(defn authenticate!
  "Exchange `public_token` from Plaid Link for an `access_token` and then
  upgrade the user to the `income` API."
  [{:keys [params identity] :as req}]
  (letfn [(-on-upgrade [_ res]
            (when-not (ok? res)
              (log-failure "Error during income upgrade" (:db/id identity) res)))
          (-on-exchange [{access-token :access_token} res]
            (if (ok? res)
              (do
                (model/create! (:db/id identity) (:public_token params) access-token)
                (service/upgrade-to-income access-token -on-upgrade))
              (log-failure "Error during public_token exchange" (:db/id identity) res)))]
    (if-let [public-token (:public_token params)]
      (do
        (service/exchange-token public-token -on-exchange)
        (ok {:message "Success!"}))
      (malformed {:message "Request must include a :public_token!"}))))

(defn hook
  "Handler for Plaid webhooks. This will be triggered some time (up to a few
  minutes) after the upgrade request has been sent, as Plaid needs to pull the
  transaction log and process it."
  [{:keys [params] :as req}]
  (letfn [(-on-income [{:keys [access_token] :as params} res]
            (let [account-id (:db/id identity)]
              (if (ok? res)
                (model/add-income-data! access_token params)
                (log-failure "Error while retrieving income information!" account-id res))))]
    (let [{:keys [code message access_token]} params]
      (info "PLAID WEBHOOK: Received request!" params)
      (condp = code
        INCOME-SUCCESS (service/get-income access_token -on-income)
        (warnf "PLAID WEBHOOK: Unrecognized status code :: code - %s :: message - %s :: account-id - %s"
               code message (:db/id identity))))))
