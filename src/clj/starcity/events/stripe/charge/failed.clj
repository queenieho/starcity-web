(ns starcity.events.stripe.charge.failed
  (:require [datomic.api :as d]
            [starcity.config :as config]
            [starcity.events.stripe.charge.common :refer :all]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [rent-payment :as rent-payment]
             [security-deposit :as security-deposit]]
            [starcity.models.stripe.customer :as customer]
            [starcity.services
             [mailgun :as mail]
             [slack :as slack]]
            [starcity.services.mailgun
             [message :as mm]
             [senders :as ms]]
            [starcity.services.slack.message :as sm]))

;; =============================================================================
;; DB

(defn failed
  "The charge has failed, so update the db."
  [conn]
  (fn [{:keys [charge]}]
    @(d/transact conn [(charge/failed-tx charge)])))

(defmulti update-db
  "Perform the necessary database updates specific to charge of `type`."
  charge-type)

(defmethod update-db :security-deposit [conn charge]
  (let [sd (security-deposit/by-charge charge)]
    (when (security-deposit/is-unpaid? sd)
      ;; TODO: Make event!
      ;; Delete the customer to kick him/her back through the onboarding flow
      (customer/delete! (-> charge charge/account account/stripe-customer)))
    @(d/transact conn [(charge/failed-tx charge)])))

(defmethod update-db :rent [conn charge]
  ;; The ACH payment failed -- user will need to try again.
  (let [p (rent-payment/by-charge conn charge)]
    @(d/transact conn [(rent-payment/due p)
                       (charge/failed-tx charge)
                       [:db/retract (:db/id p) :rent-payment/paid-on (:rent-payment/paid-on p)]])))

(defmethod update-db :default [conn charge]
  @(d/transact conn [(charge/failed-tx charge)]))

;; =============================================================================
;; Slack Notifications

(defmulti notify-internal
  "Send an internal notification that charge of `type` has failed."
  charge-type)

(defmethod notify-internal :security-deposit [conn charge]
  (let [account (charge/account charge)]
    (slack/ops
     (sm/msg
      (sm/failure
       (sm/title "Security Deposit ACH Failure")
       (sm/text (format "%s's ACH payment has failed." (account/full-name account)))
       (sm/fields
        (sm/field "Email" (account/email account) true)
        (sm/field "Phone" (account/phone-number account) true)))))))

(defmethod notify-internal :rent [conn charge]
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

(defmethod notify-internal :default [_ _]
  :noop)

;; =============================================================================
;; Email Notifications

(defmulti notify-user
  "Send a notification to the user that charge of `type` has failed."
  charge-type)

(defn- deposit-msg [account]
  (mm/msg
   (mm/greeting (account/first-name account))
   (mm/p "Unfortunately your security deposit payment failed to go through.")
   (mm/p "The most common reasons for this are insufficient funds, or incorrectly entered account credentials.")
   (mm/p "Please log back in to Starcity by clicking " [:a {:href (format "%s/onboarding" config/hostname)} "this link"] " to re-enter your bank account information.")
   (mm/signature)))

(defmethod notify-user :security-deposit [conn charge]
  (let [account (charge/account charge)]
    (mail/send (account/email account) "Security Deposit Payment Failure"
               (deposit-msg account)
               :from ms/noreply)))

(defn- rent-msg [account]
  (mm/msg
   (mm/greeting (account/first-name account))
   (mm/p "Unfortunately your rent payment has failed to go through.")
   (mm/p (format "Please log back into your <a href='%s/me/account/rent'>member dashboard</a> and try your payment again."
                 config/hostname))
   (mm/signature)))

(defmethod notify-user :rent [conn charge]
  (let [account (charge/account charge)]
    (mail/send (account/email account) "Rent Payment Failed"
               (rent-msg account)
               :from ms/noreply)))

(defmethod notify-user :default [_ _]
  :noop)
