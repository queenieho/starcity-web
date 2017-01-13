(ns starcity.events.stripe.customer
  (:require [clojure.core.async :refer [<! go]]
            [dire.core :refer [with-pre-hook!]]
            [datomic.api :as d]
            [starcity
             [config :as config]
             [datomic :refer [conn]]]
            [starcity.models.account :as account]
            [starcity.services
             [mailgun :as mail]
             [slack :as slack]]
            [starcity.services.mailgun
             [message :as mm]
             [senders :as ms]]
            [starcity.services.slack.message :as sm]
            [taoensso.timbre :as timbre]))

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

(defn- notify-internal [account]
  (slack/ops
   (sm/msg
    (sm/failure
     (sm/title "Bank Verification Failure")
     (sm/text (format "%s's bank account could not be verified."
                      (account/full-name account)))))))

(defn- lookup-customer [customer-id]
  (d/entity (d/db conn) [:stripe-customer/customer-id customer-id]))

(defn verification-failed!
  "The customer's bank verification has failed. He/she should be notified, and
  we should be notified too, because why not?"
  [customer-id]
  (go (let [customer (lookup-customer customer-id)
            account  (:stripe-customer/account customer)
            res-1    (<! (notify-user account))
            res-2    (<! (notify-internal account))]
        (if-let [error (or (:error res-1) (:error res-2))]
          (do
            (timbre/error error ::verification-failed {:customer-id customer-id})
            error)
          :ok))))

(with-pre-hook! #'verification-failed!
  (fn [c] (timbre/info ::verification-failed {:customer-id c})))
