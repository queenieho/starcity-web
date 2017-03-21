(ns starcity.api.mars.rent.payments
  (:require [compojure.core :refer [defroutes GET POST]]
            [clojure.spec :as s]
            [datomic.api :as d]
            [starcity
             [auth :as auth]
             [datomic :refer [conn]]
             [util :refer :all]]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [member-license :as member-license]
             [msg :as msg]
             [rent :as rent]
             [rent-payment :as rent-payment]]
            [starcity.models.stripe.customer :as customer]
            [starcity.services.stripe :as stripe]
            [starcity.util.response :as resp]
            [taoensso.timbre :as timbre]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Handlers
;; =============================================================================

;; =============================================================================
;; Next Payment

(defn next-payment-handler
  "Retrieve the details of the 'next' payment for requesting account."
  [{:keys [params] :as req}]
  (let [account (auth/requester req)]
    (resp/json-ok (rent/next-payment conn account))))

;; =============================================================================
;; Payments List

(defn- clientize-payment-item
  [grace-over {:keys [:rent-payment/method :rent-payment/check] :as p}]
  (let [amount   (:rent-payment/amount p)
        overdue  (rent-payment/past-due? p)
        late-fee (and grace-over overdue)]
    (merge {:id       (:db/id p)
            :status   (name (:rent-payment/status p))
            :pstart   (:rent-payment/period-start p)
            :pend     (:rent-payment/period-end p)
            :due      (:rent-payment/due-date p)
            :paid     (:rent-payment/paid-on p)
            :overdue  overdue
            :late-fee late-fee
            :amount   (if late-fee (* amount 1.1) amount)
            :desc     (:rent-payment/method-desc p)}
           (when method {:method (name method)})
           (when check {:check {:number (:check/number check)}}))))

(defn payments-handler
  "Retrieve the list of rent payments for the requesting account."
  [req]
  (let [account    (auth/requester req)
        payments   (rent/payments conn account)
        grace-over (->> (member-license/active conn account)
                        (member-license/grace-period-over? conn))]
    (resp/json-ok
     {:payments (->> (take 12 payments)
                     (map (partial clientize-payment-item grace-over)))})))

;; =============================================================================
;; Make a Payment

(defn- cents [x]
  (int (* 100 x)))

(defn- charge-amount [conn license payment]
  (if (and (rent-payment/past-due? payment)
           (member-license/grace-period-over? conn license))
    (* (rent-payment/amount payment) 1.1)
    (rent-payment/amount payment)))

(defn- create-charge!
  "Create a charge for `payment` on Stripe."
  [conn account payment license amount]
  (if (rent-payment/unpaid? payment)
    (let [customer (account/stripe-customer (d/db conn) account)]
      (get-in (stripe/charge (cents amount)
                             (customer/bank-account-token customer)
                             (account/email account)
                             :customer-id (customer/id customer)
                             :managed-account (member-license/managed-account-id license))
              [:body :id]))
    (throw (ex-info "Cannot pay a payment that is already paid!"
                    {:payment (:db/id payment) :account (:db/id account)}))))

(defn- make-payment!
  [conn account payment]
  (let [license   (member-license/active conn account)
        amount    (charge-amount conn license payment)
        charge-id (create-charge! conn account payment license amount)]
    @(d/transact conn [(assoc
                        (rent-payment/pending payment)
                        :rent-payment/amount amount
                        :rent-payment/paid-on (java.util.Date.)
                        :rent-payment/method rent-payment/ach
                        :rent-payment/charge (charge/create charge-id amount :account account))
                       (msg/ach-payment payment account)])))

(s/fdef make-payment!
        :args (s/cat :conn p/conn?
                     :account p/entity?
                     :payment p/entity?))

(def already-paid-error
  "Cannot pay a payment that has already been paid!")

(defn make-payment
  "Make an ACH account for `account` against the payment identified by `payment-id`."
  [conn account payment-id]
  (let [payment (d/entity (d/db conn) payment-id)]
    (if (rent-payment/paid? payment)
      (resp/json-unprocessable {:error already-paid-error})
      (try
        (timbre/info :mars.rent/pay-ach {:account    (account/email account)
                                         :payment-id payment-id})
        (make-payment! conn account payment)
        (resp/json-ok {:message "ok"})
        (catch Throwable t
          (timbre/error t :mars.rent/pay-ach {:account    (account/email account)
                                              :payment-id payment-id}))))))

;; =============================================================================
;; Routes
;; =============================================================================

(defroutes routes
  (GET "/" [] payments-handler)

  (GET "/next" [] next-payment-handler)

  (POST "/:payment-id/pay" [payment-id]
        (fn [req]
          (let [account (auth/requester req)]
            (make-payment conn account (str->int payment-id))))))
