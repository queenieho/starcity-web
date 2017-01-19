(ns starcity.webhooks.stripe.subscription
  (:require [datomic.api :as d]
            [starcity
             [config :as config]
             [datomic :refer [conn]]]
            [starcity.events.autopay :as autopay]
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
  (mail/send (account/email account) "Autopay Beginning Soon"
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

(defmethod handle-event "customer.subscription.deleted" [data event]
  (manage event (autopay/unsubscribe! (subscription-id data))))

(comment

  (do
    (event/create "blah" "customer.subscription.trial_will_end")
    (handle-event {:id   "blah"
                   :type "customer.subscription.trial_will_end"
                   :data {:object {:id "sub_9mToVxHJw7PJRq"}}}
                  (d/entity (d/db conn) [:stripe-event/event-id "blah"])))

  (let [account (account/by-email "member@test.com")
        license (member-license/active conn account)
        sub-id  "sub_9mUbid8jkxAQvd"]
    (event/create "blah3" "customer.subscription.deleted")
    @(d/transact conn [{:db/id (:db/id license)
                        :member-license/subscription-id sub-id}])
    (handle-event {:id   "blah3"
                   :type "customer.subscription.deleted"
                   :data {:object {:id sub-id}}}
                  (d/entity (d/db conn) [:stripe-event/event-id "blah3"])))



  )
