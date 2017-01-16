(ns starcity.events.stripe.charge.succeeded
  (:require [datomic.api :as d]
            [starcity.events.stripe.charge.common :refer :all]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [rent-payment :as rent-payment]
             [security-deposit :as security-deposit]]
            [starcity.services
             [mailgun :as mail]
             [slack :as slack]]
            [starcity.services.mailgun
             [message :as mm]
             [senders :as ms]]
            [starcity.services.slack.message :as sm]))

;; =============================================================================
;; DB

(defmulti update-db
  "Perform the necessary database updates specific to charge of `type`."
  (fn [conn charge _] (charge-type conn charge)))

(defn- new-amount
  "Given the security deposit and an amount in cents, determine what the new
  amount should be."
  [security-deposit amount]
  (let [amount-dollars (int (/ amount 100))]
    (+ (or (security-deposit/amount-received security-deposit) 0)
       amount-dollars)))

(defmethod update-db :security-deposit [conn charge amount]
  (let [sd (security-deposit/by-charge charge)]
    @(d/transact conn [(charge/succeeded-tx charge)
                       {:db/id                            (:db/id sd)
                        :security-deposit/amount-received (new-amount sd amount)}])))

;; This means that we're looking at a successful `:rent-payment` entity of
;; type ACH. Simply set its status to `:rent-payment.status/paid`.
(defmethod update-db :rent [conn charge _]
  (let [p (rent-payment/by-charge conn charge)]
    @(d/transact conn [(charge/succeeded-tx charge)
                       (rent-payment/paid p)])))

(defmethod update-db :default [conn charge _]
  @(d/transact conn [(charge/succeeded-tx charge)]))

;; =============================================================================
;; Slack Notifications

(defmulti notify-internal
  "Send an internal notification that charge of `type` has succeeded."
  charge-type)

(defmethod notify-internal :security-deposit [conn charge]
  (let [account (charge/account charge)]
    (slack/ops
     (sm/msg
      (sm/success
       (sm/title "Security Deposit Paid")
       (sm/text (format "%s has paid his/her security deposit!" (account/full-name account))))))))

(defmethod notify-internal :rent [conn charge]
  (let [account (charge/account charge)]
    (slack/ops
     (sm/msg
      (sm/success
       (sm/title "Rent Successfully Paid by ACH")
       (sm/text (format "%s's rent ACH payment has succeeded."
                        (account/full-name account))))))))

(defmethod notify-internal :default [_ _]
  :noop)

;; =============================================================================
;; Email Notifications

(defmulti notify-user
  "Send a notification to the user that charge of `type` has succeeded."
  charge-type)

(defn- deposit-msg [account]
  (mm/msg
   (mm/greeting (account/first-name account))
   (mm/p "This is a confirmation to let you know that your security deposit was successfully paid.")
   (mm/signature)))

(defmethod notify-user :security-deposit [conn charge]
  (let [account (charge/account charge)]
    (mail/send (account/email account) "Security Deposit Payment Succeeded"
               (deposit-msg account)
               :from ms/noreply)))

(defn- rent-msg [account amount]
  (mm/msg
   (mm/greeting (account/first-name account))
   (mm/p (format "This is a confirmation to let you know that your rent payment of <b>$%s</b> was successfully paid."
                 amount))
   (mm/signature)))

(defmethod notify-user :rent [conn charge]
  (let [account (charge/account charge)
        payment (rent-payment/by-charge conn charge)]
    (mail/send (account/email account) "Rent Payment Succeeded"
               (rent-msg account (int (rent-payment/amount payment)))
               :from ms/noreply)))

(defmethod notify-user :default [_ _]
  :noop)
