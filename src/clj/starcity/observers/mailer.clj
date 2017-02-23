(ns starcity.observers.mailer
  (:require [datomic.api :as d]
            [starcity.config :as config]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [member-license :as member-license]
             [msg :as msg]
             [rent-payment :as rent-payment]
             [security-deposit :as deposit]]
            [starcity.services.mailgun :as mail]
            [starcity.services.mailgun
             [message :as mm]
             [senders :as ms]]
            [taoensso.timbre :as timbre]))

(defmulti handle (fn [conn msg] (:msg/key msg)))

(defmethod handle :default
  [_ msg]
  (timbre/debug ::no-handler msg))

;; =============================================================================
;; Monthly Rent Reminders

(defn- send-reminder [account uuid]
  (mail/send
   (account/email account)
   "Reminder: Your Rent is Due"
   (mm/msg
    (mm/greeting (account/first-name account))
    (mm/p "It's that time again! Your rent payment is <b>due by the 5th</b>.")
    (mm/p "Please log into your member dashboard "
          [:a {:href (str config/hostname "/me/account/rent")} "here"]
          " to pay your rent with ACH. <b>If you'd like to stop getting these reminders, sign up for autopay while you're there!</b>")
    (mm/signature))
   :from ms/noreply
   :uuid uuid))

(defmethod handle msg/rent-payments-created-key
  [conn {license-ids :msg/params :as msg}]
  (let [accounts (map (comp member-license/account (partial d/entity (d/db conn))) license-ids)]
    (doseq [account accounts]
      (send-reminder account (:msg/uuid msg)))))

;; =============================================================================
;; Stripe
;; =============================================================================

;; =============================================================================
;; Charges

;; =====================================
;; Succeeded

(defmulti charge-succeeded
  (fn [conn charge uuid] (charge/type conn charge)))

(defmethod charge-succeeded :default [_ _ _] :noop)

(defmethod charge-succeeded :security-deposit [conn charge uuid]
  (let [account (charge/account charge)
        deposit (deposit/by-charge charge)]
    (mail/send
     (account/email account)
     "Security Deposit Payment Succeeded"
     (mm/msg
      (mm/greeting (account/first-name account))
      ;; If it's partially paid, that means that the user just paid the initial $500
      (if (deposit/partially-paid? deposit)
        (mm/p (format "This is a confirmation to let you know that your initial security deposit payment has succeeded. <b>Please note that the remainder of your deposit &mdash; $%s &mdash; is due one month from now.</b>"
                      (deposit/amount-remaining deposit)))
        (mm/p "This is a confirmation to let you know that your security deposit has been successfully paid."))
      (mm/signature))
     :from ms/noreply
     :uuid uuid)))

(defmethod charge-succeeded :rent [conn charge uuid]
  (let [account (charge/account charge)
        payment (rent-payment/by-charge conn charge)]
    (mail/send
     (account/email account)
     "Rent Payment Succeeded"
     (mm/msg
      (mm/greeting (account/first-name account))
      (mm/p (format "This is a confirmation to let you know that your rent payment of <b>$%.2f</b> was successfully paid."
                    (rent-payment/amount payment)))
      (mm/signature))
     :from ms/noreply
     :uuid uuid)))

(defmethod handle msg/charge-succeeded-key
  [conn {charge-id :msg/params :as msg}]
  (let [charge (charge/lookup conn charge-id)]
    (charge-succeeded conn charge (:msg/uuid msg))))

;; =====================================
;; Failed

(defmulti charge-failed
  (fn [conn charge uuid] (charge/type conn charge)))

(defmethod charge-failed :default [_ _ _] :noop)

(defmethod charge-failed :security-deposit [conn charge uuid]
  (let [account (charge/account charge)
        deposit (deposit/by-charge charge)]
    (mail/send
     (account/email account)
     "Security Deposit Payment Failure"
     (mm/msg
      (mm/greeting (account/first-name account))
      (mm/p "Unfortunately your security deposit payment failed to go through.")
      (mm/p "The most common reasons for this are insufficient funds, or incorrectly entered account credentials.")
      ;; If it's partially paid, that means that the user is no longer
      ;; in onboarding.
      (if (deposit/partially-paid? deposit)
        (mm/p "Please log back in to your member dashboard by clicking "
              [:a {:href (format "%s/me/account/rent" config/hostname)} "this link"]
              " to retry your payment.")
        (mm/p "Please log back in to Starcity by clicking "
              [:a {:href (format "%s/onboarding" config/hostname)} "this link"]
              " to re-enter your bank account information."))
      (mm/signature))
     :from ms/noreply
     :uuid uuid)))

(defmethod charge-failed :rent [conn charge uuid]
  (let [account (charge/account charge)]
    (mail/send
     (account/email account)
     "Rent Payment Failed"
     (mm/msg
      (mm/greeting (account/first-name account))
      (mm/p "Unfortunately your rent payment has failed to go through.")
      (mm/p (format "Please log back into your <a href='%s/me/account/rent'>member dashboard</a> and try your payment again."
                    config/hostname))
      (mm/signature))
     :from ms/noreply
     :uuid uuid)))

(defmethod handle msg/charge-failed-key
  [conn {charge-id :msg/params :as msg}]
  (let [charge (charge/lookup conn charge-id)]
    (charge-failed conn charge (:msg/uuid msg))))

;; =============================================================================
;; Customer

(defn- link [account]
  (cond
    (account/onboarding? account) (format "%s/onboarding" config/hostname)
    (account/member? account)     (format "%s/me/account/rent" config/hostname)
    :otherwise                    (throw (ex-info "Wrong role."
                                                  {:role (account/role account)}))))

(defmulti source-updated
  (fn [conn msg account]
    (get-in msg [:msg/params :status])))

(defmethod source-updated :default
  [conn msg account]
  :noop)

(defmethod source-updated "verification_failed"
  [conn msg account]
  (mail/send
   (account/email account)
   "Bank Verification Failed"
   (mm/msg
    (mm/greeting (account/first-name account))
    (mm/p "Unfortunately we were unable to make the two small deposits to the bank account you provided &mdash; it's likely that the information provided was incorrect.")
    (mm/p "Please log back in to Starcity by clicking "
          [:a {:href (link account)} "this link"]
          " to re-enter your bank account information.")
    (mm/signature))
   :from ms/noreply
   :uuid (:msg/uuid msg)))

(defmethod handle msg/customer-source-updated-key
  [conn {params :msg/params :as msg}]
  (let [{:keys [customer-id]} params
        account               (account/by-customer-id conn customer-id)]
    (source-updated conn msg account)))

;; =============================================================================
;; Invoice (Autopay)

;; =====================================
;; Created

(defmethod handle msg/invoice-created-key
  [conn {params :msg/params :as msg}]
  (let [invoice-id (:invoice-id params)
        license    (member-license/by-invoice-id conn invoice-id)
        account    (member-license/account license)]
    (mail/send
     (account/email account)
     "Autopay Payment Pending"
     (mm/msg
      (mm/greeting (account/first-name account))
      (mm/p "This is a friendly reminder that your autopay payment is being processed")
      (mm/p "Please note that it may take ub to <b>5 business days</b> for the funds to be withdrawn from your account")
      (mm/signature))
     :from ms/noreply
     :uuid (:msg/uuid msg))))

;; =====================================
;; Payment Failed

(defn- will-retry? [payment]
  (< (:rent-payment/autopay-failures payment)
     rent-payment/max-autopay-failures))

(defmethod handle msg/invoice-payment-failed-key
  [conn {invoice-id :msg/params :as msg}]
  (let [payment (rent-payment/by-invoice-id conn invoice-id)
        license (member-license/by-invoice-id conn invoice-id)
        account (member-license/account license)]
    (mail/send
     (account/email account)
     "Autopay Payment Failed"
     (mm/msg
      (mm/greeting (account/first-name account))
      (mm/p "Unfortunately, your rent payment has failed.")
      (if (will-retry? payment)
        (mm/p "We'll retry again in the next couple of days. In the meantime, please ensure that you have sufficient funds in the account that you have linked to Autopay.")
        (mm/p "We have now tried to charge you three times, and <b>we will not try again; you will need to make your payment another way.</b>"))
      (when-not (will-retry? payment)
        (mm/p (format "<b>If you wish to use Autopay in the future, you will need to subscribe to it in your <a href='%s/me/account/rent'>dashboard</a> again.</b>" config/hostname)))
      (mm/signature))
     :from ms/noreply
     :uuid (:msg/uuid msg))))

;; =====================================
;; Payment Succeeded

(defmethod handle msg/invoice-payment-succeeded-key
  [conn {invoice-id :msg/params :as msg}]
  (let [payment (rent-payment/by-invoice-id conn invoice-id)
        license (member-license/by-invoice-id conn invoice-id)
        account (member-license/account license)]
    (mail/send
     (account/email account)
     "Autopay Payment Successful"
     (mm/msg
      (mm/greeting (account/first-name account))
      (mm/p
       (format "We're just letting you know that your rent payment of $%s was successfully received."
               (int (rent-payment/amount payment))))
      (mm/p "Thanks for using Autopay!")
      (mm/signature))
     :from ms/noreply
     :uuid (:msg/uuid msg))))

;; =============================================================================
;; Subscriptions

(defmethod handle msg/autopay-will-begin-key
  [conn {license-id :msg/params :as msg}]
  (let [license (d/entity (d/db conn) license-id)
        account (member-license/account license)]
    (mail/send
     (account/email account)
     "Autopay Beginning Soon"
     (mm/msg
      (mm/greeting (account/first-name account))
      (mm/p "This is a friendly reminder that, since you configured <b>autopay</b>, your first payment will be taking place on the <b>1st of the upcoming month</b>.")
      (mm/p "For more details, log in to your Starcity account "
            [:a {:href (format "%s/me/account/rent" config/hostname)} "here"]
            ".")
      (mm/signature))
     :from ms/noreply
     :uuid (:msg/uuid msg))))

(defmethod handle msg/autopay-deactivated-key
  [conn {license-id :msg/params :as msg}]
  (let [license (d/entity (d/db conn) license-id)
        account (member-license/account license)]
    (mail/send
     (account/email account)
     "Autopay Deactivated"
     (mm/msg
      (mm/greeting (account/first-name account))
      (mm/p "We have failed to charge the account that you have linked to autopay for the third time, so <b>autopay has been deactivated for your account</b>.")
      (mm/signature))
     :from ms/noreply
     :uuid (:msg/uuid msg))))
