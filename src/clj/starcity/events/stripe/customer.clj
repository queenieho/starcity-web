(ns starcity.events.stripe.customer
  (:require [datomic.api :as d]
            [starcity
             [config :as config]
             [datomic :refer [conn]]
             [util :refer [<!?]]]
            [starcity.events.plumbing :refer [defproducer]]
            [starcity.models.account :as account]
            [starcity.services.mailgun :as mail]
            [starcity.services.mailgun
             [message :as mm]
             [senders :as ms]]))

(defn- link [account]
  (cond
    (account/onboarding? account) (format "%s/onboarding" config/hostname)
    (account/member? account)     (format "%s/me/account/rent" config/hostname)
    :otherwise                    (throw (ex-info "Wrong role." {:role (account/role account)}))))

(defn- email-content [account]
  (mm/msg
   (mm/greeting (account/first-name account))
   (mm/p "Unfortunately we were unable to make the two small deposits to the bank account you provided &mdash; it's likely that the information provided was incorrect.")
   (mm/p "Please log back in to Starcity by clicking " [:a {:href (link account)} "this link"] " to re-enter your bank account information.")
   (mm/signature)))

(defn- notify-user [account]
  (mail/send (account/email account) "Bank Verification Failed"
             (email-content account)
             :from ms/noreply))

(defn- lookup-customer [customer-id]
  (d/entity (d/db conn) [:stripe-customer/customer-id customer-id]))

;; In the case of this producer, the action being taken is to send notifications
;; to the user -- there's nothing to be done in the DB yet.
(defproducer verification-failed! ::verification-failed
  [customer-id]
  (let [account (account/by-customer-id conn customer-id)
        _       (<!? (notify-user account))]
    :done))

(comment
  (verification-failed! "cus_9bzpu7sapb8g7y")

  )
