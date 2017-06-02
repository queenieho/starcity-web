(ns starcity.models.apply.submit
  (:require [datomic.api :as d]
            [hiccup.core :refer [html]]
            [starcity
             [datomic :refer [conn]]]
            [starcity.models
             [account :as account]
             [application :as application]
             [community-safety :as community-safety]
             [stripe :as stripe]]
            [starcity.services.mailgun :as mailgun]
            [starcity.config :as config]
            [starcity.services.slack :as slack]
            [taoensso.timbre :as t]))

;; =============================================================================
;; Internal
;; =============================================================================

;; =============================================================================
;; Submission Email

(defn- submission-email-content [first-name]
  (html
   [:body
    [:p (format "Hi %s," first-name)]
    [:p "Thank you for completing Starcity's membership application. Next:"]
    [:ol
     [:li "We'll process your application (community safety and financial checks) to pre-qualify you for the community,"]
     [:li "and then notify you as soon as you're pre-qualified, and one of our community members will then reach out to schedule an interview and tour of the home you've applied for."]]
    [:p "Stay tuned and thanks for your patience!"]
    [:p "Best,"
     [:br]
     [:br]
     "Meg"
     [:br]
     "Head of Community"]]))

(def ^:private submission-email-subject
  "We Are Processing Your Application")

(defn- send-submission-email! [account]
  (try
    (mailgun/send-email (account/email account)
                        submission-email-subject
                        (submission-email-content (account/first-name account)))
    (t/info ::email {:user (account/email account)})
    (catch Exception e
      (t/error e ::email {:user (account/email account)}))))

;; =============================================================================
;; Community Safety

(defn- community-safety-check!
  "Attempt to run a community safety check for `account`."
  [account]
  (try
    (let [ent (d/entity (d/db conn) (community-safety/check! account))]
      (t/info ::community-safety {:report-url          (community-safety/report ent)
                                  :community-safety-id (:db/id ent)
                                  :user                (account/email account)}))
    (catch Exception e
      (t/error e ::community-safety {:user (account/email account)}))))

;; ;; =============================================================================
;; ;; Slack Notification

(defn- rand-doge []
  (let [phrases ["Such marketing" "Wow" "Much victory"
                 "Great success" "Very amazing"
                 "Dope" "So skilled"]]
    (->> phrases count rand-int (get phrases))))

(defn notify-us
  [account]
  (let [title (format "%s's application" (account/full-name account))
        link  (format "%s/admin/accounts/%s" config/hostname (:db/id (:account/member-application account)))]
    (slack/rich-message title "View it here on the admin dashboard."
                        :channel "#community"
                        :opts {:pretext    (format "%s! Someone signed up! :partyparrot:"
                                                   (rand-doge))
                               :title_link link
                               :color      "#7a8e52"})))

;; =============================================================================
;; Payment

(def application-fee
  "The application fee in cents."
  2500)

;; TODO: Stripe Library!
(defn- charge-application-fee!
  "Try to charge the user the $25 application fee, catching and logging any
  errors that occur, then rethrowing."
  [account token]
  (try
    (let [charge-id (stripe/create-charge! conn (:db/id account) application-fee token
                                           :description "member application fee")]
      (t/info ::application-fee {:charge-id    charge-id
                                 :user         (account/email account)
                                 :stripe-token token
                                 :amount       application-fee}))
    (catch Exception e
      (t/error e ::application-fee {:user         (account/email account)
                                    :stripe-token token
                                    :amount       application-fee})
      (throw e))))

;; =============================================================================
;; API
;; =============================================================================

(defn submit!
  "Given an account and a Stripe token, submit the application by charging
  `account` the application fee, sending a confirmation email, and
  transitioning the application status."
  [account stripe-token]
  (let [application (account/member-application account)
        ;; first, try to process the charge
        _           (charge-application-fee! account stripe-token)]
    ;; we'll only get here if no exception was thrown by the charge
    (do
      ;; Catching errors isn't crucial here -- we can always re-run the check
      ;; manually
      (community-safety-check! account)
      ;; Again, not critical. We can catch this in the logs, and this is only a
      ;; confirmation.
      (send-submission-email! account)
      (notify-us account)
      @(d/transact conn [(application/submit application)]))))
