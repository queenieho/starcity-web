(ns starcity.services.stripe.request
  (:require [cheshire.core :as json]
            [org.httpkit.client :as http]
            [starcity.config.stripe :as config]
            [starcity.services.codec :refer [form-encode]]
            [plumbing.core :refer [assoc-when]]
            [taoensso.timbre :as t]))

;; =============================================================================
;; Internal
;; =============================================================================

(def ^:private base-url
  "https://api.stripe.com/v1")

(defn- parse-json-body
  [res]
  (update-in res [:body] json/parse-string true))

(defn- inject-throw-error
  "Inspect the response and log errors if found."
  [res]
  (do
    (when-let [{:keys [type message param] :as e} (get-in res [:body :error])]
      (t/error ::request-error {:type type :message message :param param})
      (throw (ex-info "Error in Stripe request!" e)))
    res))

(defn- params-for
  [method params]
  (case method
    :get [:query-params params]
    [:body (form-encode params)]))

;; =============================================================================
;; API
;; =============================================================================

(defn error-from
  "Extract the error message from the response."
  [response]
  (get-in response [:body :error]))

(def payload-from
  "Extract the body of the response."
   :body)

(defn request
  ([req-config params]
   (request req-config params nil))
  ([{:keys [endpoint method managed-account] :as conf} params cb]
   (let [req-map    {:url        (format "%s/%s" base-url endpoint)
                     :method     method
                     :headers    (assoc-when
                                  {"Accept" "application/json"}
                                  "Stripe-Account" managed-account)
                     :basic-auth [config/secret-key ""]}
         [k params] (params-for method params)]
     (if cb
       ;; NOTE: The error handling doesn't work here, since the exception is
       ;; thrown on the thread that the cb is invoked in...which is not the
       ;; calling thread.
       (http/request (assoc req-map k params)
                     (comp cb inject-throw-error parse-json-body))
       (-> @(http/request (assoc req-map k params))
           parse-json-body
           inject-throw-error)))))
