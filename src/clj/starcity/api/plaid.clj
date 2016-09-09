(ns starcity.api.plaid
  (:require [starcity.services.plaid :as service]
            [starcity.models.plaid :as model]
            [starcity.api.common :refer :all]
            [taoensso.timbre :as timbre]))


;; =============================================================================
;; Helpers
;; =============================================================================

(defn- log-failure
  ([account-id params]
   (log-failure "Plaid failure" account-id params))
  ([message account-id {:keys [status body] :as res}]
   (timbre/warnf "PLAID: %s :: account-id - %s :: status - %s :: code - %s :: plaid message - %s :: resolve - %s"
          message account-id status (:code body) (:message body) (:resolve body))))

;; =============================================================================
;; API
;; =============================================================================

(defn verify-income
  "Exchange `public_token` from Plaid Link for an `access_token` and then
  upgrade the user to the `income` API."
  [{:keys [params identity] :as req}]
  (letfn [(-on-upgrade [res]
            (when-not (ok? res)
              (log-failure "Error during income upgrade" (:db/id identity) res)))
          (-on-exchange [{:keys [body] :as res}]
            (if (ok? res)
              (let [{access-token :access_token} body]
                (model/create! (:db/id identity) (:public_token params) access-token)
                (service/upgrade-to-income access-token -on-upgrade))
              (log-failure "Error during public_token exchange" (:db/id identity) res)))]
    (if-let [public-token (:public_token params)]
      (do
        (service/exchange-token public-token :cb -on-exchange)
        (ok {:message "Success!"}))
      (malformed {:message "Request must include a :public_token!"}))))
