(ns starcity.services.plaid
  (:require [starcity.services.common :as common]
            [starcity.services.codec :refer [form-encode]]
            [starcity.config :refer [config]]
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

;; (defn- inject-log-error
;;   "Inspect response and log errors if found."
;;   [res]
;;   (do
;;     (when-let [{:keys []}])
;;     res))

(defn- plaid-request
  ([req-config token params]
   (plaid-request req-config token params nil))
  ([{:keys [endpoint token-key webhook?] :or {token-key :access_token}} token params cb]
   (let [{:keys [secret client-id env webhook]} (:plaid config)
         options                                (when webhook? {:webhook webhook})
         url                                    (format "%s/%s" (base-url env) endpoint)
         body                                   (-> (merge {:secret    secret
                                                            :client_id client-id
                                                            token-key  token}
                                                           params)
                                                    (assoc-when :options options)
                                                    json/generate-string)
         req-map                                {:body body :headers {"Content-Type" "application/json"}}]
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

(defn get-income
  "Get income data for user."
  [access-token & [cb]]
  (plaid-request {:endpoint "income/get"}
                 access-token
                 {}
                 cb))

(defn auth-data
  "Get auth data for a user."
  [access-token & [cb]]
  (plaid-request {:endpoint "auth/get"}
                 access-token
                 {}
                 cb))

(defn upgrade-to-income
  "Upgrade user to be an income user."
  [access-token & [cb]]
  (plaid-request {:endpoint "upgrade"
                  :webhook? true}
                 access-token
                 {:upgrade_to "income"}
                 cb))

(comment

  (doseq [access-token []]
    @(http/post "https://joinstarcity.com/webhooks/plaid"
                {:body (json/generate-string {:code 10
                                              :message "testing..."
                                              :access_token access-token})
                 :headers {"Content-Type" "application/json"}}))

  )
