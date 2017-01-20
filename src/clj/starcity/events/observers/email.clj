(ns starcity.events.observers.email
  (:require [clojure.core.async :refer [chan sliding-buffer]]
            [datomic.api :as d]
            [starcity
             [config :as config]
             [datomic :refer [conn]]]
            [starcity.events.plumbing :refer [defobserver]]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [member-license :as member-license]
             [rent-payment :as rent-payment]]
            [starcity.services.mailgun :as mail]
            [starcity.services.mailgun
             [message :as mm]
             [senders :as ms]]
            [taoensso.timbre :as timbre]
            [starcity.models.security-deposit :as security-deposit]))

;; =============================================================================
;; Handlers
;; =============================================================================

(defmulti handler :event)

(defmethod handler :default
  [{event :event}]
  (timbre/debug "unhandled event" event))

;; =============================================================================
;; Rent

(defn- send-reminder [account]
  (mail/send (account/email account) "Reminder: Your Rent is Due"
             (mm/msg
              (mm/greeting (account/first-name account))
              (mm/p "It's that time again! Your rent payment is <b>due by the 5th</b>.")
              (mm/p "Please log into your member dashboard "
                    [:a {:href (str config/hostname "/me/account/rent")} "here"]
                    " to pay your rent with ACH. <b>If you'd like to stop getting these reminders, sign up for autopay while you're there!</b>")
              (mm/signature))
             :from ms/noreply))

(defn- send-reminders [conn license-ids]
  (let [accounts (map (comp member-license/account (partial d/entity (d/db conn))) license-ids)]
    (doseq [account accounts]
      (send-reminder account))))

(defmethod handler :starcity.events.rent/create-monthly-rent-payments
  [{:keys [licenses] :as event}]
  (send-reminders conn licenses))

;; =============================================================================
;; Stripe: Charges

(defmulti charge-failed charge/type)
(defmethod charge-failed :default [_ c] :noop)

(defmethod charge-failed :security-deposit [conn charge]
  (let [account (charge/account charge)
        deposit (security-deposit/by-charge charge)]
    (mail/send (account/email account) "Security Deposit Payment Failure"
               (mm/msg
                (mm/greeting (account/first-name account))
                (mm/p "Unfortunately your security deposit payment failed to go through.")
                (mm/p "The most common reasons for this are insufficient funds, or incorrectly entered account credentials.")
                ;; If it's partially paid, that means that the user is no longer
                ;; in onboarding.
                (if (security-deposit/partially-paid? deposit)
                  (mm/p "Please log back in to your member dashboard by clicking "
                        [:a {:href (format "%s/me/account/rent" config/hostname)} "this link"]
                        " to retry your payment.")
                  (mm/p "Please log back in to Starcity by clicking "
                        [:a {:href (format "%s/onboarding" config/hostname)} "this link"]
                        " to re-enter your bank account information."))
                (mm/signature))
               :from ms/noreply)))

(defmethod charge-failed :rent [conn charge]
  (let [account (charge/account charge)]
    (mail/send (account/email account) "Rent Payment Failed"
               (mm/msg
                (mm/greeting (account/first-name account))
                (mm/p "Unfortunately your rent payment has failed to go through.")
                (mm/p (format "Please log back into your <a href='%s/me/account/rent'>member dashboard</a> and try your payment again."
                              config/hostname))
                (mm/signature))
               :from ms/noreply)))

(defmethod handler :starcity.events.stripe.charge/failed
  [{:keys [charge-id]}]
  (let [charge (charge/lookup charge-id)]
    (charge-failed conn charge)))

;; =====================================
;; Success

(defmulti charge-succeeded charge/type)
(defmethod charge-succeeded :default [_ _] :noop)

(defmethod charge-succeeded :security-deposit [conn charge]
  (let [account (charge/account charge)
        deposit (security-deposit/by-charge charge)]
    (mail/send (account/email account) "Security Deposit Payment Succeeded"
               (mm/msg
                (mm/greeting (account/first-name account))
                ;; If it's partially paid, that means that the user just paid the initial $500
                (if (security-deposit/partially-paid? deposit)
                  (mm/p (format "This is a confirmation to let you know that your initial security deposit payment has succeeded. <b>Please note that the remainder of your deposit &mdash; $%s &mdash; is due one month from now."
                                (security-deposit/amount-remaining deposit)))
                  (mm/p "This is a confirmation to let you know that your security deposit has been successfully paid."))
                (mm/signature))
               :from ms/noreply)))

(defmethod charge-succeeded :rent [conn charge]
  (let [account (charge/account charge)
        payment (rent-payment/by-charge conn charge)]
    (mail/send (account/email account) "Rent Payment Succeeded"
               (mm/msg
                (mm/greeting (account/first-name account))
                (mm/p (format "This is a confirmation to let you know that your rent payment of <b>$%.2f</b> was successfully paid."
                              (rent-payment/amount payment)))
                (mm/signature))
               :from ms/noreply)))

(defmethod handler :starcity.events.stripe.charge/succeeded
  [{:keys [charge-id]}]
  (let [charge (charge/lookup charge-id)]
    (charge-succeeded conn charge)))

;; =============================================================================
;; Stripe: Invoice

;; =====================================
;; Payment Failed

(defn- payment-failure-msg [account failures]
  (let [will-retry (< failures 3)]
    (mm/msg
     (mm/greeting (account/first-name account))
     (mm/p "Unfortunately, your rent payment has failed.")
     (if will-retry
       (mm/p "We'll retry again in the next couple of days. In the meantime, please ensure that you have sufficient funds in the account that you have linked to Autopay.")
       (mm/p "We have now tried to charge you three times, and <b>we will not try again; you will need to make your payment another way.</b>"))
     (when-not will-retry
       (mm/p "If you wish to use Autopay in the future, <b>you will need to subscribe to it in your <a href='https://joinstarcity.com/me/account/rent'>dashboard</a> again.</b>"))
     (mm/signature))))

(defmethod handler :starcity.events.stripe.invoice/payment-failed
  [{:keys [invoice-id]}]
  (let [license  (member-license/by-invoice-id conn invoice-id)
        account  (member-license/account license)
        failures (-> (rent-payment/by-invoice-id conn invoice-id) rent-payment/failures)]
    (mail/send (account/email account) "Autopay Payment Failed"
               (payment-failure-msg account failures)
               :from ms/noreply)))

;; =====================================
;; Payment Succeeded

(defn- payment-success-msg [account payment]
  (mm/msg
   (mm/greeting (account/first-name account))
   (mm/p
    (format "We're just letting you know that your rent payment of $%s was successfully made."
            (int (rent-payment/amount payment))))
   (mm/p "Thanks for using Autopay!")
   (mm/signature)))

(defmethod handler :starcity.events.stripe.invoice/payment-succeeded
  [{:keys [invoice-id]}]
  (let [license (member-license/by-invoice-id conn invoice-id)
        account (member-license/account license)
        payment (rent-payment/by-invoice-id conn invoice-id)]
    (mail/send (account/email account) "Autopay Payment Successful"
               (payment-success-msg account payment)
               :from ms/noreply)))

;; =============================================================================
;; Autopay

(defn- deactivation-email [account]
  (mm/msg
   (mm/greeting (account/first-name account))
   (mm/p "We have failed to charge the account that you have linked to autopay for the third time, so autopay has been deactivated for your account.")
   (mm/signature)))

(defmethod handler :starcity.events.autopay/unsubscribe
  [{:keys [license]}]
  (let [account (member-license/account license)]
    (mail/send (account/email account) "Autopay Deactivated"
               (deactivation-email account)
               :from ms/noreply)))

;; =============================================================================
;; Observer
;; =============================================================================

(defobserver email (chan (sliding-buffer 4096)) handler)
