(ns starcity.controllers.auth.signup
  (:require [starcity.views.auth.signup :as view]
            [starcity.controllers.utils :refer :all]
            [starcity.models.account :as account]
            [starcity.services.mailgun :refer [send-email]]
            [starcity.config :refer [config]]
            [starcity.datomic :refer [conn]]
            [buddy.auth :refer [authenticated?]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.string :refer [trim capitalize lower-case]]
            [ring.util.response :as response]
            [ring.util.codec :refer [url-encode]]
            [datomic.api :as d]))

;; =============================================================================
;; Constants
;; =============================================================================

(def ^:private +redirect-after-activation+ "/availability")
(def ^:private +redirect-after-signup+ "/signup/complete")

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

(defn- show-signup* [req & {:keys [errors email first-name last-name]
                            :or   {errors     []
                                   email      ""
                                   first-name ""
                                   last-name  ""}}]
  (view/signup errors email first-name last-name))

;; =============================================================================
;; Activation

;; TODO: 'Resend' mechanism
(defn- show-invalid-activation
  [req]
  (ok (view/invalid-activation)))

(defn- send-activation-email
  [user-id]
  (let [pattern [:account/email :account/first-name :account/last-name :account/activation-hash]
        {:keys [account/email
                account/first-name
                account/last-name
                account/activation-hash]} (d/pull (d/db conn) pattern user-id)]
    (send-email email "Starcity Account Activation"
                (format "Hello %s %s,\n%s/signup/activate?email=%s&hash=%s"
                        first-name
                        last-name
                        (:hostname config)
                        (url-encode email)
                        activation-hash))))

;; =============================================================================
;; API
;; =============================================================================

;; =============================================================================
;; Complete

(defn show-complete [req]
  (ok (view/signup-complete)))

;; =============================================================================
;; Signup

(defn show-signup [req]
  (if (authenticated? req)
    (response/redirect "/application")
    (ok (show-signup* req))))

(defn signup! [{:keys [params] :as req}]
  (letfn [(-respond-malformed [& errors]
            (let [{:keys [first-name last-name email]} params]
              (malformed (show-signup* req :errors errors
                                       :email email
                                       :first-name first-name
                                       :last-name last-name))))]
    (if-let [params (matching-passwords? params)]
      (let [vresult (-> params clean-params validate)]
        (if-let [{:keys [email password first-name last-name]} (valid? vresult)]
          (if-not (account/exists? email)
            ;; SUCCESS
            (let [uid (account/create! email password first-name last-name)]
              (do
                (send-activation-email uid)
                (response/redirect +redirect-after-signup+)))
            ;; account already exists for email
            (-respond-malformed (format "An account is already registered for %s." email)))
          ;; validation failure
          (apply -respond-malformed (errors-from vresult))))
      ;; passwords don't match
      (-respond-malformed (format "Those passwords do not match. Please try again.")))))

;; =============================================================================
;; Activation

(defn activate! [{:keys [params session] :as req}]
  (let [{:keys [email hash]} params]
    (if (or (nil? email) (nil? hash))
      (show-invalid-activation req)
      (let [user (account/by-email email)]
        (if (= hash (:account/activation-hash user))
          (let [_       (account/activate! user)
                session (assoc session :identity (account/by-email email))]
            (-> (response/redirect +redirect-after-activation+)
                (assoc :session session)))
          ;; hashes don't match
          (show-invalid-activation req))))))
