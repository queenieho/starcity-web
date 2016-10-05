(ns starcity.services.stripe
  (:require [starcity.services.stripe.request :refer [request]]
            [plumbing.core :refer [assoc-when]]
            [clojure.spec :as s]))

;; =============================================================================
;; API
;; =============================================================================

(s/def ::config
  (s/keys :req-un [::secret-key ::public-key]))

(defn error-from
  "Extract the error message from the response."
  [response]
  (get-in response [:body :error]))

(def payload-from
  "Extract the body of the response."
  :body)

(defn fetch-customer
  "Retrieve a customer from the Stripe API."
  [customer-id & {:keys [cb]}]
  (request {:endpoint   (format "customers/%s" customer-id)
            :method     :get}
           {}
           cb))

(defn create-customer
  "Create a new Stripe customer."
  [email source & {:keys [cb description]}]
  (request {:endpoint   "customers"
            :method     :post}
           (assoc-when
            {:email  email
             :source source}
            :description description)
           cb))

(defn verify-source
  "Verify a bank account with microdeposits."
  [customer-id bank-account-token amount-1 amount-2 & {:keys [cb]}]
  (request {:endpoint (format "customers/%s/sources/%s/verify"
                              customer-id bank-account-token)
            :method   :post}
           {:amounts [amount-1 amount-2]}
           cb))

(s/fdef verify-source
        :args (s/cat :customer-id string?
                     :bank-account-token string?
                     :amount-1 integer?
                     :amount-2 integer?
                     :opts (s/keys* :opt-un [::cb])))

(defn charge
  [amount source email & {:keys [cb description customer-id managed-account]}]
  (request {:endpoint "charges"
            :method   :post}
           (-> {:amount      amount
                :source      source
                :currency    "usd"
                :description description
                :destination managed-account}
               (assoc-when :customer customer-id))
           cb))

(comment

  (create-customer "josh@joinstarcity.com"
                   "btok_96U3zx8CJVi3Tf"
                   "test to determine structure of bank account source")

  (fetch-customer "cus_96V5gpDRHp4BP9")


  )
