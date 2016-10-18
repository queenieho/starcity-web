(ns starcity.controllers.signup
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [buddy.auth :refer [authenticated?]]
            [clojure.string :refer [capitalize lower-case trim]]
            [datomic.api :as d]
            [hiccup.core :refer [html]]
            [ring.util
             [codec :refer [url-encode]]
             [response :as response]]
            [starcity
             [config :as config]
             [datomic :refer [conn]]]
            [starcity.controllers.utils :refer :all]
            [starcity.models.account :as account]
            [starcity.services.mailgun :refer [send-email]]
            [starcity.views.signup :as view]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Constants
;; =============================================================================

(def redirect-after-signup "/signup/complete")
(def redirect-after-activation "/apply")

;; =============================================================================
;; Helpers
;; =============================================================================

;; =============================================================================
;; Signup

(defn- validate
  [params]
  (b/validate
   params
   {:email      [(required "An email address is required.")
                 [v/email :message "That email address is invalid."]]
    :password   [(required "A password is required.")
                 [v/min-count 8 :message "Your password should be at least 8 characters long."]]
    :first-name [(required "Please enter your first name.")]
    :last-name  [(required "Please enter your last name.")]}))

(defn- clean-params
  [params]
  (let [tc (comp trim capitalize)]
    (-> (update params :email (comp trim lower-case))
        (update :password trim)
        (update :first-name tc)
        (update :last-name tc))))

(defn- matching-passwords?
  [{:keys [password-1 password-2] :as params}]
  (when (= password-1 password-2)
    (assoc params :password password-1)))

;; =============================================================================
;; Activation

;; TODO: 'Resend' mechanism
(defn- show-invalid-activation
  [req]
  (ok (view/invalid-activation req)))

(defn- log-activation [email]
  (infof "EMAIL - [ACTIVATION] - sending activation email to '%s'" email))

(defn- activation-email-content
  [{:keys [:account/email :account/first-name :account/activation-hash]}]
  (html
   [:body
    [:p (format "Hi %s," first-name)]
    [:p "Thank you for signing up! "
     [:a {:href (format "%s/signup/activate?email=%s&hash=%s"
                        config/hostname
                        (url-encode email)
                        activation-hash)}
      "Click here to activate your account"]
     " and apply for a home."]
    [:p "Best," [:br] [:br] "Mo" [:br] "Head of Community"]]))

(def ^:private activation-email-subject
  "Activate Your Account")

(defn- send-activation-email
  [account-id]
  (let [acct  (d/entity (d/db conn) account-id)
        email (:account/email acct)]
    (log-activation email)
    (send-email email
                activation-email-subject
                (activation-email-content acct))))

;; =============================================================================
;; API
;; =============================================================================

;; =============================================================================
;; Complete

(defn show-complete [req]
  (ok (view/complete req)))

;; =============================================================================
;; Signup

;; TODO: shouldn't need to do this here...seems like a job for middleware
(defn show-signup [req]
  (if (authenticated? req)
    (response/redirect "/apply")
    (ok (view/signup req))))

(defn signup! [{:keys [params] :as req}]
  (if-let [params (matching-passwords? params)]
    (let [vresult (-> params clean-params validate)]
      (if-let [{:keys [email password first-name last-name]} (valid? vresult)]
        (if-not (account/exists? email)
          ;; SUCCESS
          (let [uid (account/create! email password first-name last-name)]
            (do
              (send-activation-email uid)
              (response/redirect redirect-after-signup)))
          ;; account already exists for email
          (respond-with-errors req (format "An account is already registered for %s." email) view/signup))
        ;; validation failure
        (respond-with-errors req (errors-from vresult) view/signup)))
    ;; passwords don't match
    (respond-with-errors req "Those passwords do not match. Please try again." view/signup)))

;; =============================================================================
;; Activation

(defn activate! [{:keys [params session] :as req}]
  (let [{:keys [email hash]} params]
    (if (or (nil? email) (nil? hash))
      (show-invalid-activation req)
      (let [acct (account/by-email email)]
        (if (= hash (:account/activation-hash acct))
          (let [session (assoc session :identity (account/session-data acct))]
            (account/activate! acct)
            (-> (response/redirect redirect-after-activation)
                (assoc :session session)))
          ;; hashes don't match
          (show-invalid-activation req))))))
