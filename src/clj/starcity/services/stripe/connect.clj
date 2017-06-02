(ns starcity.services.stripe.connect
  (:require [starcity.services.stripe.request :refer [request]]))

(defn create-token
  "Create a bank account token that can be attached to a managed account
  customer."
  [customer-id bank-token managed-account-id]
  (request {:endpoint        "tokens"
            :method          :post
            :managed-account managed-account-id}
           {:customer     customer-id
            :bank_account bank-token}))
