(ns starcity.events.observers.slack
  (:require [clj-time
             [coerce :as c]
             [format :as f]]
            [clojure.core.async :refer [chan sliding-buffer]]
            [starcity.datomic :refer [conn]]
            [starcity.events.plumbing :refer [defobserver]]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [rent-payment :as rent-payment]]
            [starcity.services.slack :as slack]
            [starcity.services.slack.message :as sm]
            [taoensso.timbre :as timbre]
            [starcity.models.member-license :as member-license]
            [starcity.models.security-deposit :as security-deposit]
            [starcity.services.stripe :as stripe]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- short-date [d]
  (f/unparse (f/formatter :date) (c/to-date-time d)))

;; =============================================================================
;; Handlers
;; =============================================================================

(defmulti handler :event)

(defmethod handler :default
  [event]
  (timbre/debug "unhandled event" (:event event)))

;; =============================================================================
;; Rent

(defmethod handler :starcity.events.rent/make-ach-payment
  [{:keys [charge-id account payment]}]
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

;; =============================================================================
;; Security Deposit

(defmethod handler :starcity.events.security-deposit/pay-remainder-ach
  [{:keys [account charge-id]}]
  (assert (and account charge-id))      ; TODO: spec
  (slack/ops
   (sm/msg
    (sm/success
     (sm/title "View Payment on Stripe"
               (format "https://dashboard.stripe.com/payments/%s" charge-id))
     (sm/text (format "%s has paid the remainder of his/her security deposit"
                      (account/full-name account)))
     (sm/fields
      (sm/field "Method" "ACH" true))))))

;; =============================================================================
;; Stripe: Charges

;; TODO: Fix Stripe API to be async
(defn- charge-amount [charge]
  (-> (:charge/stripe-id charge) stripe/fetch-charge :body :amount))

;; =====================================
;; Failure

(defmulti charge-failed charge/type)
(defmethod charge-failed :default [_ _] :noop)

(defmethod charge-failed :security-deposit
  [conn charge]
  (let [account      (charge/account charge)
        deposit      (security-deposit/by-charge charge)
        is-remainder (security-deposit/partially-paid? deposit)
        amount       (charge-amount charge)]
    (slack/ops
     (sm/msg
      (sm/failure
       (sm/title "Security Deposit ACH Failure")
       (sm/text (format "%s's ACH payment has failed." (account/full-name account)))
       (sm/fields
        (sm/field "Email" (account/email account) true)
        (sm/field "Phone" (account/phone-number account) true)
        (sm/field "Payment" (if is-remainder "remainder" "initial") true)
        (sm/field "Amount" (format "$%.2f" (float (/ amount 100))) true)))))))

(defmethod charge-failed :rent [conn charge]
  (let [account (charge/account charge)]
    (slack/ops
     (sm/msg
      (sm/failure
       (sm/title "ACH Rent Payment Failed")
       (sm/text (format "%s's rent payment has failed to go through."
                        (account/full-name account)))
       (sm/fields
        (sm/field "Email" (account/email account) true)
        (sm/field "Phone" (account/phone-number account) true)))))))

(defmethod handler :starcity.events.stripe.charge/failed
  [{:keys [charge-id]}]
  (let [charge (charge/lookup charge-id)]
    (charge-failed conn charge)))

;; =====================================
;; Success

(defmulti charge-succeeded charge/type)
(defmethod charge-succeeded :default [_ _] :noop)

(defmethod charge-succeeded :security-deposit
  [conn charge]
  (let [account    (charge/account charge)
        deposit    (security-deposit/by-charge charge)
        is-initial (security-deposit/partially-paid? deposit)
        amount     (charge-amount charge)]
    (slack/ops
     (sm/msg
      (sm/success
       (sm/title "Security Deposit Succeeded")
       (sm/text (format "%s's security deposit payment has succeeded."
                        (account/full-name account)))
       (sm/fields
        (sm/field "Payment" (if is-initial "initial" "remainder") true)
        (sm/field "Amount" (format "$%.2f" (float (/ amount 100))) true)))))))

(defmethod charge-succeeded :rent
  [conn charge]
  (let [account (charge/account charge)]
    (slack/ops
     (sm/msg
      (sm/success
       (sm/title "Rent Successfully Paid by ACH")
       (sm/text (format "%s's rent ACH payment has succeeded."
                        (account/full-name account))))))))

(defmethod handler :starcity.events.stripe.charge/succeeded
  [{:keys [charge-id]}]
  (let [charge (charge/lookup charge-id)]
    (charge-succeeded conn charge)))

;; =============================================================================
;; Stripe: Customer

(defmethod handler :starcity.events.stripe.customer/verification-failed
  [{:keys [customer-id]}]
  (let [account (account/by-customer-id conn customer-id)]
    (slack/ops
     (sm/msg
      (sm/failure
       (sm/title "Bank Verification Failure")
       (sm/text (format "%s's bank account could not be verified."
                        (account/full-name account))))))))

;; =============================================================================
;; Stripe: Invoice

(defn- invoice-dashboard-url [managed-id invoice-id]
  (format "https://dashboard.stripe.com/%s/invoices/%s" managed-id invoice-id) )

;; =====================================
;; Payment failed

(defmethod handler :starcity.events.stripe.invoice/payment-failed
  [{:keys [invoice-id]}]
  (let [failures (rent-payment/failures (rent-payment/by-invoice-id conn invoice-id))
        license  (member-license/by-invoice-id conn invoice-id)
        account  (member-license/account license)
        managed  (member-license/managed-account-id license)]
    (slack/ops
     (sm/msg
      (sm/failure
       (sm/title "View Invoice on Stripe" (invoice-dashboard-url managed invoice-id))
       (sm/text (format "%s's autopay payment has failed" (account/full-name account)))
       (sm/fields
        (sm/field "Attempts" failures true)))))))

;; =====================================
;; Payment failed

(defmethod handler :starcity.events.stripe.invoice/payment-succeeded
  [{:keys [invoice-id]}]
  (let [license  (member-license/by-invoice-id conn invoice-id)
        account  (member-license/account license)
        managed  (member-license/managed-account-id license)]
    (slack/ops
     (sm/msg
      (sm/success
       (sm/title "View Invoice on Stripe" (invoice-dashboard-url managed invoice-id))
       (sm/text (format "%s's autopay payment has succeeded!" (account/full-name account))))))))

;; =============================================================================
;; Observer
;; =============================================================================

(defobserver slack (chan (sliding-buffer 4096)) handler)
