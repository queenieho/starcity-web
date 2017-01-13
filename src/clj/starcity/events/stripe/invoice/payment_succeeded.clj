(ns starcity.events.stripe.invoice.payment-succeeded
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

(defn paid!
  "Indicate that this payment has been paid."
  [conn payment]
  @(d/transact conn [(assoc
                      (rent-payment/paid payment)
                      :rent-payment/paid-on (java.util.Date.))]))

(defn slack
  "Notify us that the rent payment was successfully made."
  [invoice-id license account]
  (let [managed (managed-account license)]
    (slack/ops
     (sm/msg
      (sm/success
       (sm/title "View Invoice on Stripe" (dashboard-url managed invoice-id))
       (sm/text (format "%s's rent payment has succeeded!" (account/full-name account))))))))

(defn- payment-success-msg [account payment]
  (mm/msg
   (mm/greeting (account/first-name account))
   (mm/p
    (format "We're just letting you know that your rent payment of $%s was successfully made."
            (int (rent-payment/amount payment))))
   (mm/p "Thanks for using Autopay!")
   (mm/signature)))

(defn notify-customer
  "Notify the customer that his/her rent payment succeeded."
  [account payment]
  (mail/send (account/email account) "Autopay Payment Successful"
             (payment-success-msg account payment)
             :from ms/noreply))
