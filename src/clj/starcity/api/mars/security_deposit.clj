(ns starcity.api.mars.security-deposit
  (:require [compojure.core :refer [defroutes GET POST]]
            [starcity
             [auth :as auth]
             [util :refer [<!!?]]]
            [starcity.api.common :refer :all]
            [starcity.events.security-deposit :refer [pay-remainder!]]
            [starcity.models
             [account :as account]
             [security-deposit :as security-deposit]]
            [starcity.models.stripe.customer :as customer]))

(defn- clientize-security-deposit [e]
  {:id       (:db/id e)
   :due-by   (security-deposit/due-by e)
   :pending  (security-deposit/amount-pending e)
   :received (security-deposit/amount-received e)
   :required (security-deposit/amount-required e)})

(defn fetch-security-deposit
  "Fetch requester's security deposit information."
  [req]
  (let [account (auth/requester req)]
    (ok {:result (-> account
                     security-deposit/by-account
                     clientize-security-deposit)})))

(defn pay-security-deposit
  "Pay requester's security deposit iff he/she has linked bank account."
  [req]
  (let [account  (auth/requester req)
        deposit  (security-deposit/by-account account)
        customer (account/stripe-customer account)]
    (cond
      (not (customer/has-verified-bank-account? customer))
      (unprocessable {:error "Cannot pay security deposit without a linked bank account."})

      (security-deposit/paid-in-full? deposit)
      (unprocessable {:error "Security deposit is already paid."})

      :otherwise (let [_ (<!!? (pay-remainder! deposit))]
                   (ok {:message "ok"})))))

(defroutes routes
  (GET "/" [] fetch-security-deposit)

  (POST "/pay" [] pay-security-deposit))
