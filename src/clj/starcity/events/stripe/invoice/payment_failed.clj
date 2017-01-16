(ns starcity.events.stripe.invoice.payment-failed
  (:require [datomic.api :as d]
            [starcity.events.stripe.invoice.common :refer :all]
            [starcity.models
             [account :as account]
             [rent-payment :as rent-payment]]
            [starcity.services
             [mailgun :as mail]
             [slack :as slack]]
            [starcity.services.mailgun
             [message :as mm]
             [senders :as ms]]
            [starcity.services.slack.message :as sm]))

(defn failed!
  "Keep track of then number of times that a payment has failed and change the
  status back to `due` when the payment has failed three times."
  [conn invoice-id payment failures]
  @(d/transact conn [(-> {:db/id                         (:db/id payment)
                          :rent-payment/autopay-failures failures}
                         (merge (when (= 3 failures))
                                {:rent-payment/status :rent-payment.status/due}))]))

(defn slack
  "Send an internal Slack notification to the #ops channel."
  [invoice-id account license failures]
  (let [managed (managed-account license)]
    (slack/ops
     (sm/msg
      (sm/failure
       (sm/title "View Invoice on Stripe" (dashboard-url managed invoice-id))
       (sm/text (format "%s's rent payment has failed" (account/full-name account)))
       (sm/fields
        (sm/field "Attempts" failures true)))))))

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

(defn notify-customer
  "Notify the customer that his/her payment has failed."
  [account failures]
  (mail/send (account/email account) "Rent Payment Failed"
             (payment-failure-msg account failures)
             :from ms/noreply))
