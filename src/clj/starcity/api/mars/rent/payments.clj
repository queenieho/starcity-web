(ns starcity.api.mars.rent.payments
  (:require [blueprints.models.account :as account]
            [blueprints.models.charge :as charge]
            [blueprints.models.customer :as customer]
            [blueprints.models.member-license :as member-license]
            [blueprints.models.property :as property]
            [blueprints.models.rent-payment :as rent-payment]
            [clojure.spec :as s]
            [compojure.core :refer [defroutes GET POST]]
            [datomic.api :as d]
            [reactor.events :as events]
            [ribbon.charge :as rc]
            [starcity.config :as config :refer [config]]
            [starcity.datomic :refer [conn]]
            [starcity.models.rent :as rent]
            [starcity.util.request :as req]
            [starcity.util.response :as resp]
            [taoensso.timbre :as timbre]
            [toolbelt.async :refer [<!!?]]
            [toolbelt.core :as tb]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Handlers
;; =============================================================================

;; =============================================================================
;; Next Payment


(defn next-payment-handler
  "Retrieve the details of the 'next' payment for requesting account."
  [{:keys [params] :as req}]
  (let [account (req/requester (d/db conn) req)]
    (resp/json-ok (rent/next-payment (d/db conn) account))))


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
           (when-some [m method] {:method (name m)})
           (when-some [c check] {:check {:number (:check/number check)}}))))

(defn payments-handler
  "Retrieve the list of rent payments for the requesting account."
  [req]
  (let [account    (req/requester (d/db conn) req)
        payments   (rent/payments (d/db conn) account)
        grace-over (->> (member-license/active (d/db conn) account)
                        (member-license/grace-period-over?))]
    (resp/json-ok
     {:payments (->> (take 12 payments)
                     (map (partial clientize-payment-item grace-over)))})))

;; =============================================================================
;; Make a Payment

(defn- cents [x]
  (int (* 100 x)))

(defn- charge-amount [license payment]
  (if (and (rent-payment/past-due? payment)
           (member-license/grace-period-over? license))
    (* (rent-payment/amount payment) 1.1)
    (rent-payment/amount payment)))

(defn- create-charge!
  "Create a charge for `payment` on Stripe."
  [customer account payment license amount]
  (if (rent-payment/unpaid? payment)
    (let [property (member-license/property license)
          desc     (format "%s's rent at %s" (account/full-name account) (property/name property))]
      (:id (<!!? (rc/create! (config/stripe-private-key config)
                             (cents amount)
                             (customer/bank-token customer)
                             :email (account/email account)
                             :description desc
                             :customer-id (customer/id customer)
                             :managed-account (member-license/managed-account-id license)))))
    (throw (ex-info "Cannot pay a payment that is already paid!"
                    {:payment (:db/id payment) :account (:db/id account)}))))

(defn- make-payment!
  [conn account payment]
  (let [license   (member-license/active (d/db conn) account)
        customer  (customer/by-account (d/db conn) account)
        amount    (charge-amount license payment)
        charge-id (create-charge! customer account payment license amount)]
    @(d/transact conn [(assoc
                        (rent-payment/set-pending payment)
                        :rent-payment/amount amount
                        :rent-payment/paid-on (java.util.Date.)
                        :rent-payment/method rent-payment/ach
                        :rent-payment/charge (charge/create account charge-id amount))
                       (events/rent-payment-made account payment)])))

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
          (let [account (req/requester (d/db conn) req)]
            (make-payment conn account (tb/str->int payment-id))))))
