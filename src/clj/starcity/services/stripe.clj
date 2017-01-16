(ns starcity.services.stripe
  (:require [starcity.services.stripe.request :refer [request]]
            [plumbing.core :refer [assoc-when]]
            [clojure.spec :as s]))

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

(defn fetch-customer
  "Retrieve a customer from the Stripe API."
  [customer-id & {:keys [cb]}]
  (request {:endpoint (format "customers/%s" customer-id)
            :method   :get}
           {}
           cb))

(defn create-customer
  "Create a new Stripe customer."
  [email source & {:keys [cb description managed-account]}]
  (request (assoc-when
            {:endpoint "customers"
             :method   :post}
            :managed-account managed-account)
           (assoc-when
            {:email  email
             :source source}
            :description description)
           cb))

(defn delete-customer
  "Create a new Stripe customer."
  [customer-id & {:keys [cb]}]
  (request {:endpoint (format "customers/%s" customer-id)
            :method   :delete}
           {}
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
           (-> {:amount   amount
                :source   source
                :currency "usd"}
               (assoc-when :customer customer-id
                           :description description
                           :destination managed-account))
           cb))

(defn fetch-charge
  [charge-id & {:keys [cb]}]
  (request {:endpoint (format "charges/%s" charge-id)
            :method   :get}
           {}
           cb))
