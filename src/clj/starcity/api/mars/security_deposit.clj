(ns starcity.api.mars.security-deposit
  (:require [compojure.core :refer [defroutes GET POST]]
            [datomic.api :as d]
            [starcity
             [auth :as auth]
             [datomic :refer [conn]]]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [member-license :as member-license]
             [msg :as msg]
             [security-deposit :as deposit]]
            [starcity.models.stripe.customer :as customer]
            [starcity.services.stripe :as stripe]
            [starcity.util.response :as resp]))

;; =============================================================================
;; Fetch

(defn- clientize-security-deposit [e]
  {:id       (:db/id e)
   :due-by   (deposit/due-by e)
   :pending  (deposit/amount-pending e)
   :received (deposit/amount-received e)
   :required (deposit/amount-required e)})

(defn fetch-security-deposit
  "Fetch requester's security deposit information."
  [req]
  (let [account (auth/requester req)]
    (resp/json-ok {:result (-> account
                               deposit/by-account
                               clientize-security-deposit)})))

;; =============================================================================
;; Pay Remainder

(def invalid-customer-error
  "Cannot pay security deposit without a linked bank account.")

(def already-paid-error
  "Your security deposit is already paid.")

(defn- charge-amount [deposit]
  (- (deposit/amount-required deposit)
     (deposit/amount-received deposit)))

(defn- make-charge!
  [deposit customer account license amount]
  (-> (stripe/charge (int (* 100 amount))
                     (customer/bank-account-token customer)
                     (account/email account)
                     :customer-id (customer/id customer)
                     :managed-account (member-license/managed-account-id license))
      (get-in [:body :id])))

(defn pay-security-deposit
  "Pay requester's security deposit iff he/she has linked bank account."
  [conn account]
  (let [deposit  (deposit/by-account account)
        customer (account/stripe-customer account)
        license  (member-license/active conn account)]
    (cond
      (nil? customer)
      (resp/json-unprocessable {:error invalid-customer-error})

      (not (customer/has-verified-bank-account? customer))
      (resp/json-unprocessable {:error invalid-customer-error})

      (deposit/paid-in-full? deposit)
      (resp/json-unprocessable {:error already-paid-error})

      :otherwise (let [amount    (charge-amount deposit)
                       charge-id (make-charge! deposit customer account license amount)
                       charge    (charge/create charge-id (float amount) :account account)]
                   @(d/transact conn [{:db/id                    (:db/id deposit)
                                       :security-deposit/charges charge}
                                      (msg/remainder-deposit-paid account charge-id)])
                   (resp/json-ok {:result "ok"})))))

(defroutes routes
  (GET "/" [] fetch-security-deposit)

  (POST "/pay" []
        (fn [req]
          (let [account (auth/requester req)]
            (pay-security-deposit conn account)))))
