(ns starcity.events.rent.make-ach-payment
  (:require [clj-time
             [coerce :as c]
             [format :as f]]
            [datomic.api :as d]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [member-license :as member-license]
             [property :as property]
             [rent-payment :as rent-payment]
             [unit :as unit]]
            [starcity.models.stripe.customer :as customer]
            [starcity.services
             [slack :as slack]
             [stripe :as stripe]]
            [starcity.services.slack.message :as sm]))

;;; Datomic Transaction

(defn- managed-account-id [conn account]
  (-> (member-license/active conn account)
      (member-license/unit)
      (unit/property)
      (property/managed-account-id)))

(defn create-payment [conn charge-id account payment]
  (let [charge (charge/create charge-id account)]
    @(d/transact conn [charge
                       (assoc
                        (rent-payment/pending payment)
                        :rent-payment/paid-on (java.util.Date.)
                        :rent-payment/method rent-payment/ach
                        :rent-payment/charge (:db/id charge))])))

;;; Create Charge

(defn- cents [x]
  (int (* 100 x)))

(defn create-charge!
  [conn account payment]
  (if (rent-payment/unpaid? payment)
    (let [customer (account/stripe-customer account)]
      (get-in (stripe/charge (cents (rent-payment/amount payment))
                             (customer/bank-account-token customer)
                             (account/email account)
                             :customer-id (customer/id customer)
                             :managed-account (managed-account-id conn account))
              [:body :id]))
    (throw (ex-info "Cannot pay a payment that is already paid!" {:payment (:db/id payment)
                                                                  :account (:db/id account)}))))

;;; Slack

(defn- short-date [d]
  (f/unparse (f/formatter :date) (c/to-date-time d)))

(defn slack
  [charge-id account payment]
  (slack/ops
   (sm/msg
    (sm/success
     (sm/title "View Payment on Stripe"
               (format "https://dashboard.stripe.com/payments/%s" charge-id))
     (sm/text (format "%s has paid his/her rent via ACH" (account/full-name account)))
     (sm/fields
      (sm/field "Amount"
                (str "$" (rent-payment/amount payment))
                true)
      (sm/field "Period Start"
                (short-date (rent-payment/period-start payment))
                true)
      (sm/field "Period End"
                (short-date (rent-payment/period-end payment))
                true))))))
