(ns starcity.services.stripe
  (:require [org.httpkit.client :as http]
            [starcity.config :refer [config]]
            [taoensso.timbre :refer [warnf warn]]
            [starcity.services.codec :refer [form-encode]]
            [cheshire.core :as json]
            [plumbing.core :refer [assoc-when]]
            [clojure.spec :as s]))

(declare error-from)

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
    (when-let [{:keys [type message param]} (error-from res)]
      (warnf "[STRIPE] %s - type: %s - param: %s" message type param))
    res))

(defn- params-for
  [method params]
  (case method
    :get [:query-params params]
    [:body (form-encode params)]))

(defn- stripe-request
  ([req-config params]
   (stripe-request req-config params nil))
  ([{:keys [endpoint method]} params cb]
   (let [{secret-key :secret-key} (:stripe config)
         req-map                  {:url        (format "%s/%s" base-url endpoint)
                                   :method     method
                                   :headers    {"Accept" "application/json"}
                                   :basic-auth [secret-key ""]}
         [k params]               (params-for method params)]
     (if cb
       (http/request (assoc req-map k params)
                     (comp cb inject-log-error parse-json-body))
       (-> @(http/request (assoc req-map k params))
           parse-json-body
           inject-log-error)))))

;; =============================================================================
;; API
;; =============================================================================

(s/def ::config
  (s/keys :req-un [::secret-key ::public-key]))

(defn error-from
  [response]
  (get-in response [:body :error]))

(def payload-from :body)

(defn fetch-customer
  "Retrieve a customer from the Stripe API."
  [customer-id & {:keys [cb]}]
  (stripe-request {:endpoint (format "customers/%s" customer-id)
                   :method   :get}
                  {}
                  cb))

(defn fetch-source
  [customer-id source-id & {:keys [cb]}]
  (stripe-request {:endpoint (format "customers/%s/sources/%s" customer-id source-id)
                   :method   :get}
                  {}
                  cb))

(defn create-customer
  "Create a new Stripe customer."
  [email source & {:keys [cb description]}]
  (stripe-request {:endpoint "customers"
                   :method   :post}
                  (assoc-when
                   {:email       email
                    :source      source}
                   :description description)
                  cb))

(defn verify-source
  "Verify a bank account with microdeposits."
  [customer-id bank-account-token amount-1 amount-2 & {:keys [cb]}]
  (stripe-request {:endpoint (format "customers/%s/sources/%s/verify"
                                     customer-id bank-account-token)
                   :method :post}
                  {:amounts [amount-1 amount-2]}
                  cb))

(s/fdef verify-source
        :args (s/cat :customer-id string?
                     :bank-account-token string?
                     :amount-1 integer?
                     :amount-2 integer?
                     :opts (s/keys* :opt-un [::cb])))

(defn charge
  [amount source email & {:keys [cb description customer-id]}]
  (stripe-request {:endpoint "charges" :method :post}
                  (-> {:amount   amount
                       :source      source
                       :currency    "usd"
                       :description description}
                      (assoc-when :customer customer-id))
                  cb))

(comment

  (create-customer "josh@joinstarcity.com"
                   "btok_96U3zx8CJVi3Tf"
                   "test to determine structure of bank account source")

  (fetch-customer "cus_96V5gpDRHp4BP9")


  )
