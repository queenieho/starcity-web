(ns starcity.services.stripe.request
  (:require [cheshire.core :as json]
            [org.httpkit.client :as http]
            [starcity.config.stripe :as config]
            [starcity.log :as log]
            [starcity.services.codec :refer [form-encode]]))

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
      (log/warn ::request-error {:type type :message message :param param}))
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
