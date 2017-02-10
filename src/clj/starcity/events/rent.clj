(ns starcity.events.rent
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure.spec :as s]
            [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.events.plumbing :refer [defproducer]]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [member-license :as member-license]
             [rent-payment :as rent-payment]]
            [starcity.models.stripe.customer :as customer]
            [starcity.services.stripe :as stripe]
            [starcity.util :refer :all]))

;; =============================================================================
;; Make ACH Payment

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
    (let [customer (account/stripe-customer account)]
      (get-in (stripe/charge (cents amount)
                             (customer/bank-account-token customer)
                             (account/email account)
                             :customer-id (customer/id customer)
                             :managed-account (member-license/managed-account-id license))
              [:body :id]))
    (throw (ex-info "Cannot pay a payment that is already paid!"
                    {:payment (:db/id payment) :account (:db/id account)}))))

(defn- create-payment
  "Create/update necessary database entities to record the payment."
  [conn charge-id account payment amount]
  (let [charge (charge/create charge-id account)]
    @(d/transact conn [charge
                       (assoc
                        (rent-payment/pending payment)
                        :rent-payment/amount amount
                        :rent-payment/paid-on (java.util.Date.)
                        :rent-payment/method rent-payment/ach
                        :rent-payment/charge (:db/id charge))])))

(defproducer make-ach-payment! ::make-ach-payment
  [account payment]
  (let [license   (member-license/active conn account)
        amount    (charge-amount conn license payment)
        charge-id (create-charge! conn account payment license amount)]
    {:result    (create-payment conn charge-id account payment amount)
     :charge-id charge-id
     :account   account
     :payment   payment}))

(s/fdef make-ach-payment!
        :args (s/cat :account entity? :payment entity?)
        :ret chan?)

;; =============================================================================
;; Create Monthly Rent Payments

(defn- query-active-licenses [conn]
  (d/q '[:find ?e ?p
         :where
         ;; active licenses
         [?e :member-license/active true]
         [?e :member-license/price ?p]
         ;; not on autopay
         [(missing? $ ?e :member-license/subscription-id)]]
       (d/db conn)))

(defn- period-end [t]
  (-> t c/to-date-time t/last-day-of-the-month c/to-date))

(defn- license-txes [start licenses]
  (mapv
   (fn [[e amount]]
     (let [p (rent-payment/create amount start (period-end start) :rent-payment.status/due)]
       {:db/id                        e
        :member-license/rent-payments p}))
   licenses))

;; Once per month all of the rent payments need to be created for active
;; members that are not on Autopay -- this event creates those payments and
;; sends email reminders to the members.
(defproducer create-monthly-rent-payments! ::create-monthly-rent-payments
  [period]
  (let [txes (->> (query-active-licenses conn) (license-txes period))]
    {:result   @(d/transact conn txes)
     :licenses (map :db/id txes)}))

(comment
  (create-monthly-rent-payments!
   (c/to-date (t/date-time 2017 2 1)))

  )
