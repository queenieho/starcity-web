(ns starcity.api.mars.security-deposit
  (:require [blueprints.models.account :as account]
            [blueprints.models.charge :as charge]
            [blueprints.models.customer :as customer]
            [blueprints.models.member-license :as member-license]
            [blueprints.models.security-deposit :as deposit]
            [compojure.core :refer [defroutes GET POST]]
            [datomic.api :as d]
            [reactor.events :as events]
            [ribbon.charge :as rc]
            [starcity.config :as config :refer [config]]
            [starcity.datomic :refer [conn]]
            [starcity.util.request :as request]
            [starcity.util.response :as resp]
            [toolbelt.async :refer [<!!?]]))

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
  (let [account (request/requester (d/db conn) req)]
    (resp/json-ok {:result (-> account deposit/by-account clientize-security-deposit)})))

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
  (:id (<!!? (rc/create! (config/stripe-private-key config)
                         (int (* 100 amount))
                         (customer/bank-token customer)
                         :description (format "security deposit payment for '%s'"
                                              (account/email account))
                         :email (account/email account)
                         :customer-id (customer/id customer)
                         :managed-account (member-license/managed-account-id license)))))

(defn pay-security-deposit
  "Pay requester's security deposit iff he/she has linked bank account."
  [conn account]
  (let [deposit  (deposit/by-account account)
        customer (customer/by-account (d/db conn) account)
        license  (member-license/active (d/db conn) account)]
    (cond
      (nil? customer)
      (resp/json-unprocessable {:error invalid-customer-error})

      (not (customer/has-verified-bank-account? customer))
      (resp/json-unprocessable {:error invalid-customer-error})

      (deposit/paid-in-full? deposit)
      (resp/json-unprocessable {:error already-paid-error})

      :otherwise (let [amount    (charge-amount deposit)
                       charge-id (make-charge! deposit customer account license amount)
                       charge    (charge/create account charge-id (float amount))]
                   @(d/transact conn [{:db/id                    (:db/id deposit)
                                       :security-deposit/charges charge}
                                      (events/remainder-deposit-payment-made account charge-id)])
                   (resp/json-ok {:result "ok"})))))

(defroutes routes
  (GET "/" [] fetch-security-deposit)

  (POST "/pay" []
        (fn [req]
          (let [account (request/requester (d/db conn) req)]
            (pay-security-deposit conn account)))))
