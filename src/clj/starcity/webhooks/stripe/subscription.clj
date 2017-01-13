(ns starcity.webhooks.stripe.subscription
  (:require [clojure.core.async :refer [go]]
            [datomic.api :as d]
            [dire.core :refer [with-pre-hook!]]
            [starcity
             [config :as config]
             [datomic :refer [conn]]]
            [starcity.models
             [account :as account]
             [member-license :as member-license]]
            [starcity.models.stripe.event :as event]
            [starcity.services.mailgun :as mail]
            [starcity.services.mailgun
             [message :as mm]
             [senders :as ms]]
            [starcity.webhooks.stripe.common :refer :all]
            [taoensso.timbre :as timbre]))

;; =============================================================================
;; Helpers

(defn- subscription-id [data]
  (get-in data [:data :object :id]))

;; =============================================================================
;; End of Subscription Trial
;; =============================================================================

(defn- autopay-starting-email [account]
  (mm/msg
   (mm/greeting (account/first-name account))
   (mm/p "This is a friendly reminder that, since you configured <b>autopay</b>, your first payment will be taking place on the <b>1st of the upcoming month</b>.")
   (mm/p "For more details, log in to your Starcity account " [:a {:href (format "%s/me/account/rent" config/hostname)} "here"] ".")
   (mm/signature)))

(defn- notify-autopay-starting [account]
  (mail/send (account/email account) "Autopay Payment Beginning Soon"
             (autopay-starting-email account)
             :from ms/noreply))

;; NOTE: See https://www.masteringmodernpayments.com/stripe-webhook-event-cheatsheet#9
(defmethod handle-event "customer.subscription.trial_will_end" [data event]
  (let [sub-id         (subscription-id data)
        member-license (member-license/by-subscription-id conn sub-id)
        account        (member-license/account member-license)]
    (timbre/info ::autopay-starting {:subscription-id sub-id
                                     :account         (account/email account)})
    (manage event (notify-autopay-starting account))))

;; =============================================================================
;; Subscription Deleted
;; =============================================================================

;; After three failed payment attempts, Stripe just deletes the subscription.
;; We'll delete the reference to the subscription on our end, which will require
;; the user to re-setup autopay should she/he wish to.

(defn- deactivation-email [account]
  (mm/msg
   (mm/greeting (account/first-name account))
   (mm/p "We have failed to charge the account that you have linked to autopay for the third time, so autopay has been deactivated for your account.")
   (mm/signature)))

(defn- notify-autopay-deactivation [account]
  (mail/send (account/email account) "Autopay Deactivated"
             (deactivation-email account)
             :from ms/noreply))

;; NOTE: This currently just removes the subscription, which puts user back
;; into the "authorization" state. Is that sufficient? Should we delete the
;; source from the Stripe customer?
(defn- delete-subscription! [sub-id]
  (go
    (try
      (let [member-license (member-license/by-subscription-id conn sub-id)
            account (member-license/account member-license)
            res @(d/transact conn [(member-license/remove-subscription member-license)])]
        (notify-autopay-deactivation account)
        res)
      (catch Throwable ex
        (timbre/error ex ::autopay-deactivated {:subscription-id sub-id})
        ex))))

(with-pre-hook! #'delete-subscription!
  (fn [s] (timbre/info ::autopay-deactivated {:subscription-id s})))

(defmethod handle-event "customer.subscription.deleted" [data event]
  (let [sub-id (subscription-id data)]
    (manage event (delete-subscription! sub-id))))

(comment

  (do
    (event/create "blah" "customer.subscription.trial_will_end")
    (handle-event {:id   "blah"
                   :type "customer.subscription.trial_will_end"
                   :data {:object {:id "sub_9mToVxHJw7PJRq"}}}
                  (d/entity (d/db conn) [:stripe-event/event-id "blah"])))

  (do
    (event/create "blah3" "customer.subscription.deleted")
    (handle-event {:id   "blah2"
                   :type "customer.subscription.deleted"
                   :data {:object {:id "sub_9mUbid8jkxAQvd"}}}
                  (d/entity (d/db conn) [:stripe-event/event-id "blah3"])))

  )
