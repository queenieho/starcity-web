(ns starcity.services.stripe.request
  (:require [org.httpkit.client :as http]
            [starcity.config.stripe :as config]
            [starcity.services.codec :refer [form-encode]]
            [cheshire.core :as json]
            [taoensso.timbre :refer [warnf warn]]))

;; =============================================================================
;; Internal
;; =============================================================================

(def ^:private base-url
  "https://api.stripe.com/v1")

(defn- parse-json-body
  [res]
  (update-in res [:body] json/parse-string true))

(defn- inject-log-error
  "Inspect the response and log errors if found."
  [res]
  (do
    (when-let [{:keys [type message param]} (get-in res [:body :error])]
      (warnf "[STRIPE] %s - type: %s - param: %s" message type param))
    res))

(defn- params-for
  [method params]
  (case method
    :get [:query-params params]
    [:body (form-encode params)]))

;; =============================================================================
;; API
;; =============================================================================

(defn request
  ([req-config params]
   (request req-config params nil))
  ([{:keys [endpoint method] :as conf} params cb]
   ;; TODO: Remove the overridable secret key, as we won't be using this
   (let [req-map    {:url        (format "%s/%s" base-url endpoint)
                     :method     method
                     :headers    {"Accept" "application/json"}
                     :basic-auth [config/secret-key ""]}
         [k params] (params-for method params)]
     (if cb
       (http/request (assoc req-map k params)
                     (comp cb inject-log-error parse-json-body))
       (-> @(http/request (assoc req-map k params))
           parse-json-body
           inject-log-error)))))
