(ns starcity.controllers.signup
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [buddy.auth :refer [authenticated?]]
            [clojure.string :refer [capitalize lower-case trim]]
            [hiccup.core :refer [html]]
            [ring.util
             [codec :refer [url-encode]]
             [response :as response]]
            [starcity
             [config :as config]]
            [starcity.controllers.utils :refer :all]
            [starcity.models.account :as account]
            [starcity.services.mailgun :refer [send-email]]
            [starcity.views.signup :as view]
            [starcity.web.messages :refer [respond-with-errors]]
            [taoensso.timbre :as timbre]))

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

(defn- activation-email-content [account]
  (html
   [:body
    [:p (format "Hi %s," (account/first-name account))]
    [:p "Thank you for signing up! "
     [:a {:href (format "%s/signup/activate?email=%s&hash=%s"
                        config/hostname
                        (url-encode (account/email account))
                        (account/activation-hash account))}
      "Click here to activate your account"]
     " and apply for a home."]
    [:p "Best," [:br] [:br] "Meg" [:br] "Head of Community"]]))

(def ^:private activation-email-subject
  "Activate Your Account")

(defn- send-activation-email [account]
  (let [email (account/email account)]
    (timbre/info ::send-activation-email {:user email})
    (send-email email
                activation-email-subject
                (activation-email-content account))))

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

(defn signup [{:keys [params] :as req}]
  (if-let [params (matching-passwords? params)]
    (let [vresult (-> params clean-params validate)]
      (if-let [{:keys [email password first-name last-name]} (valid? vresult)]
        (if-not (account/exists? email)
          ;; SUCCESS
          (let [account (account/create email password first-name last-name)]
            (do
              (timbre/info :account/created {:user       email
                                             :first-name first-name
                                             :last-name  last-name})
              (send-activation-email account)
              (response/redirect redirect-after-signup)))
          ;; account already exists for email
          (respond-with-errors req (format "An account is already registered for %s." email) view/signup))
        ;; validation failure
        (respond-with-errors req (errors-from vresult) view/signup)))
    ;; passwords don't match
    (respond-with-errors req "Those passwords do not match. Please try again." view/signup)))

;; =============================================================================
;; Activation

(defn activate [{:keys [params session] :as req}]
  (let [{:keys [email hash]} params]
    (if (or (nil? email) (nil? hash))
      (show-invalid-activation req)
      (let [acct (account/by-email email)]
        (if (= hash (account/activation-hash acct))
          (let [session (assoc session :identity (account/session-data acct))]
            (do
              (account/activate acct)
              (timbre/info :account/activated {:user email})
              (-> (response/redirect redirect-after-activation)
                  (assoc :session session))))
          ;; hashes don't match
          (show-invalid-activation req))))))
