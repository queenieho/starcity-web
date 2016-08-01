(ns starcity.controllers.application.submit
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [ring.util.response :as response]
            [hiccup.core :refer [html]]
            [starcity.controllers.application.common :as common]
            [starcity.controllers.utils :refer :all]
            [starcity.models
             [account :as account]
             [application :as application]
             [community-safety :as community-safety]]
            [starcity.datomic :refer [conn]]
            [starcity.services.stripe :as stripe]
            [starcity.services.mailgun :as mailgun]
            [starcity.services.community-safety :refer [background-check]]
            [starcity.views.application.submit :as view]
            [taoensso.timbre :as timbre]
            [datomic.api :as d]))

(timbre/refer-timbre)

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- can-view-submit?
  [{:keys [identity] :as req}]
  (when-let [application-id (:db/id (application/by-account-id (:db/id identity)))]
    (application/community-fitness-complete? application-id)))

(defn show-submit*
  [{:keys [identity] :as req} & {:keys [errors] :or []}]
  (let [current-steps (application/current-steps (:db/id identity))]
    (view/submit current-steps (:account/email identity) errors)))

(defn- payment-error [req]
  (malformed (show-submit* req :errors ["Something went wrong while processing your payment. Please try again."])))

(defn- charge-application-fee [token email]
  (stripe/charge 2500 token email))

;; =============================================================================
;; Parameter Validation

(defn- validate-params
  "Validate that user has accepted terms of service, allowed us to run a
  background check, and that the Stripe payment worked."
  [params]
  (let [tos-msg    "You must acknowledge the terms of service to proceed."
        bgc-msg    "You must allow us to run a background check in order to become part of a Starcity community."
        stripe-msg "Something went wrong while processing your payment. Please try again."]
    (b/validate
     params
     {:tos-acknowledged      [(required tos-msg) [v/member #{"on"} :message tos-msg]]
      :background-permission [(required bgc-msg) [v/member #{"on"} :message bgc-msg]]
      :stripe-token          [[v/string :message stripe-msg] (required stripe-msg)]})))

;; =============================================================================
;; Submission Email

(defn- submission-email-content
  [first-name]
  (html
   [:body
    [:p (format "Hi %s," first-name)]
    [:p "Thank you for completing Starcity's membership application. Here's what to expect now:"]
    [:ol
     [:li "Over the next couple of business days, we'll process your application (community safety and financial checks) to pre-qualify you for the community."]
     [:li "We'll notify you as soon as you're pre-qualified, and one of our community members will then reach out to schedule an interview and tour of the home you've applied for."]]
    [:p "Stay tuned and thanks for your patience!"]
    [:p "Best,"
     [:br]
     "Team Starcity"]]))

(defn- send-submission-email
  [account-id]
  (let [{:keys [:account/email :account/first-name]}
        (d/pull (d/db conn) [:account/email :account/first-name] account-id)]
    (mailgun/send-email email "We Are Processing Your Application"
                        (submission-email-content first-name))))

;; =============================================================================
;; API
;; =============================================================================

(defn show-submit
  "Show the submit page."
  [req]
  (ok (show-submit* req)))

(defn- pull-background-information
  "Pull the requisite/availabile information needed to run a background check."
  [account-id]
  (let [pattern [:db/id
                 :account/first-name
                 :account/last-name
                 :account/middle-name
                 :account/dob
                 :account/email
                 {:account/member-application
                  [{:member-application/current-address
                    [:address/state :address/city :address/postal-code]}]}]]
    (d/pull (d/db conn) pattern account-id)))

(defn handle-background-check-result
  "Handle Community Safety response, dispatching on status code."
  [account-id wants-report? {:keys [headers status body]}]
  (if (= status 201)
    (do
      (infof "Successfully ran background check! account-id: %d" account-id)
      (community-safety/create account-id (:location headers) wants-report?))
    (warnf "Error while running background check! account-id: %d -- error code: %d -- body: %s"
           account-id status body)))

(defn- run-background-check
  "Run a background check for user identified by account-id."
  [account-id wants-report?]
  (-> (pull-background-information account-id)
      (background-check
       (partial handle-background-check-result account-id wants-report?))))

(defn- is-on?
  [checkbox-answer]
  (= checkbox-answer "on"))

(defn submit!
  [{:keys [identity params] :as req}]
  (let [{:keys [db/id account/email]} identity
        vresult                       (validate-params params)]
    (if-let [{:keys [stripe-token receive-background-check]} (valid? vresult)]
      (let [{:keys [status body] :as res} (charge-application-fee stripe-token email)]
        (if (= status 200)
          (do
            (run-background-check id (is-on? receive-background-check))
            (send-submission-email id)
            (application/complete! id (:id body))
            (response/redirect "/application?completed=true"))
          (payment-error req)))
      (payment-error req))))

(def restrictions
  (common/restrictions can-view-submit?))
