(ns starcity.services.plaid
  (:require [cheshire.core :as json]
            [mount.core :as mount :refer [defstate]]
            [org.httpkit.client :as http]
            [starcity.config :refer [config]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- plaid-request
  [{:keys [endpoint token-key params webhook?] :or {token-key :access_token params {}}}
   {:keys [secret client-id env webhook]}]
  (fn [token cb]
    (let [env  (if (= env "production") "api" env)
          url  (format "https://%s.plaid.com/%s" env endpoint)
          body (merge {:secret    secret
                       :client_id client-id
                       token-key  token} params)]
      (do
        (infof "PLAID: POSTing to %s :: params - %s" url params)
        (http/post url {:body    (json/generate-string (if webhook?
                                                         (assoc body :options {:webhook webhook})
                                                         body))
                        :headers {"Content-Type" "application/json"}}
                   (fn [res]
                     (let [res (update-in res [:body] json/parse-string true)]
                       (cb (:body res) res))))))))

(def ^:private exchange-token-request
  "Exchange `public_token` for `access_token.`"
  (partial plaid-request {:endpoint "exchange_token" :token-key :public_token}))

(def ^:private income-request
  "Get income data for user."
  (partial plaid-request {:endpoint "income/get"}))

(def ^:private upgrade-income-request
  "Upgrade user to become an income user."
  (partial plaid-request {:endpoint "upgrade"
                    :params   {:upgrade_to "income"}
                    :webhook? true}))

;; =============================================================================
;; API
;; =============================================================================

(defstate exchange-token :start (exchange-token-request (:plaid config)))
(defstate get-income :start (income-request (:plaid config)))
(defstate upgrade-to-income :start (upgrade-income-request (:plaid config)))
