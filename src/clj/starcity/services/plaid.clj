(ns starcity.services.plaid
  (:require [starcity.services.common :as common]
            [starcity.services.codec :refer [form-encode]]
            [starcity.config :as config :refer [config]]
            [org.httpkit.client :as http]
            [taoensso.timbre :as timbre]
            [cheshire.core :as json]
            [plumbing.core :refer [assoc-when]]
            [clojure.spec :as s]))

(timbre/refer-timbre)

;; =============================================================================
;; Internal
;; =============================================================================

(defn- base-url
  "The base url for Plaid requests."
  [env]
  (let [env' (if (= env "production")
               "api"
               env)]
    (format "https://%s.plaid.com" env')))

(s/fdef base-url
        :args (s/cat :env #{"production" "tartan"})
        :ret string?)

(defn- plaid-request
  ([req-config token params]
   (plaid-request req-config token params nil))
  ([{:keys [endpoint token-key webhook?] :or {token-key :access_token}} token params cb]
   (let [options (when webhook? {:webhook (config/plaid-webhook-url config)})
         url     (format "%s/%s" (base-url (config/plaid-env config)) endpoint)
         body    (-> (merge {:secret    (config/plaid-secret-key config)
                             :client_id (config/plaid-client-id config)
                             token-key  token}
                            params)
                     (assoc-when :options options)
                     json/generate-string)
         req-map {:body body :headers {"Content-Type" "application/json"}}]
     (if cb
       (http/post url req-map (comp cb common/parse-json-body))
       (-> @(http/post url req-map)
           common/parse-json-body)))))

;; =============================================================================
;; API
;; =============================================================================

(defn exchange-token
  "Exchange `public_token` for `access_token.`"
  [public-token & {:keys [cb account-id]}]
  (plaid-request {:endpoint  "exchange_token"
                  :token-key :public_token}
                 public-token
                 (assoc-when {} :account_id account-id)
                 cb))

(defn auth-data
  "Get auth data for a user."
  [access-token & [cb]]
  (plaid-request {:endpoint "auth/get"}
                 access-token
                 {}
                 cb))
