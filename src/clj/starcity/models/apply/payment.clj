(ns starcity.models.apply.payment
  (:require [starcity.models
             [account :as account]
             [community-safety :as community-safety]
             [stripe :as stripe]]
            [starcity.models.apply.common :as common]
            [starcity.services
             [slack :as slack]
             [mailgun :as mailgun]]
            [starcity.config :refer [config]]
            [starcity.datomic :refer [conn]]
            [hiccup.core :refer [html]]
            [taoensso.timbre :refer [infof errorf]]
            [datomic.api :as d]))

;; =============================================================================
;; Internal
;; =============================================================================

;; =============================================================================
;; Submission Email

(defn- submission-email-content
  [first-name]
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
     "Mo"
     [:br]
     "Head of Community"]]))

(defn- send-submission-email
  [account-id]
  (let [{:keys [:account/email :account/first-name]}
        (d/pull (d/db conn) [:account/email :account/first-name] account-id)]
    (try
      (mailgun/send-email email "We Are Processing Your Application"
                          (submission-email-content first-name))
      (catch Exception e
        (errorf e "Error encountered while sending submission email to %s" email)))))

;; =============================================================================
;; Community Safety

(defn- community-safety-check [account-id]
  (try
    (community-safety/check account-id)
    (catch Exception _)))

;; =============================================================================
;; Slack Notification

(defn- rand-doge []
  (let [phrases ["Such marketing" "Wow" "Much victory"
                 "Great success" "Very amazing"
                 "Dope" "So skilled"]]
    (->> phrases count rand-int (get phrases))))

(defn notify-us
  [account-id]
  (let [acct  (d/entity (d/db conn) account-id)
        title (format "%s's application" (account/full-name acct))
        link  (format "%s/admin/applications/%s" (:hostname config) account-id)]
    (slack/rich-message title "View it here on the admin dashboard."
                        :channel "#members"
                        :opts {:pretext    (format "%s! Someone signed up! :partyparrot:"
                                                   (rand-doge))
                               :title_link link
                               :color      "#7a8e52"})))

;; =============================================================================
;; Payment

(def application-fee
  "The application fee in cents."
  2500)

(defn- charge-application-fee
  "Try to charge the user the $25 application fee, catching and logging any
  errors that occur, then rethrowing."
  [account-id token]
  (try
    (stripe/create-charge account-id application-fee token
                          :description "member application fee")
    (catch Exception e
      (errorf e "Error encountered while attempting to charge application fee!")
      (throw e))))

;; =============================================================================
;; Lock Application

(defn- submit-application [application-id]
  @(d/transact
    conn
    [{:db/id                           application-id
      :member-application/status       :member-application.status/submitted
      :member-application/submitted-at (java.util.Date.)}]))

;; =============================================================================
;; API
;; =============================================================================

;; NOTE: Seems failure-prone.
(defn submit-payment
  "Submit payment and lock the member's application."
  [account-id stripe-token]
  (let [application-id (:db/id (common/by-account-id account-id))
        ;; first try to process the charge
        _              (charge-application-fee account-id stripe-token)]
    ;; we'll only get here if no exception was thrown by the charge
    (do
      (community-safety-check account-id)
      (send-submission-email account-id)
      (notify-us account-id)
      (submit-application application-id))))
