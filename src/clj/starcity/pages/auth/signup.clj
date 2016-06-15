(ns starcity.pages.auth.signup
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [malformed ok]]
            [starcity.pages.auth.common :refer :all]
            [starcity.router :refer [route]]
            [starcity.models.account :as account]
            [starcity.services.mailgun :refer [send-email]]
            [starcity.config :refer [config]]
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
;; Components
;; =============================================================================

(defn- form-group
  [id label attrs]
  [:div.form-group
   [:label.control-label.col-sm-2 {:for id} label]
   [:div.col-sm-10
    [:input.form-control (merge {:id id} attrs)]]])

(defn- signup-content
  [req errors email first-name last-name]
  [:div.container
   [:form.form-horizontal {:action "/signup" :method "post"}
    [:h2 "Sign Up"]
    (for [e errors]
      [:div.alert.alert-danger {:role "alert"} e])

    (form-group "input-email" "Email"
                {:name        "email"
                 :type        "email"
                 :placeholder "Email address"
                 :required    true
                 :autofocus   (when (empty? email) true)
                 :value       email})

    (form-group "input-first-name" "First Name"
                {:name        "first-name"
                 :type        "text"
                 :placeholder "First Name"
                 :required    true
                 :value       first-name})

    (form-group "input-last-name" "Last Name"
                {:name        "last-name"
                 :type        "text"
                 :placeholder "Last Name"
                 :required    true
                 :value       last-name})

    (form-group "input-password-1" "Password"
                {:name        "password-1"
                 :type        "password"
                 :placeholder "Password"
                 :required    true})

    (form-group "input-password-2" "Re-enter Password"
                {:name        "password-2"
                 :type        "password"
                 :placeholder "Re-enter password"
                 :required    true})

    [:div.form-group
     [:div.col-sm-offset-2.col-sm-10
      [:button.btn.btn-default {:type "submit"} "Sign Up"]]]]])

;; =============================================================================
;; Signup Validation

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
;; API
;; =============================================================================

;; =============================================================================
;; Complete

(defn render-complete [req]
  (ok (base [:h2 "Check your inbox, yo."])))

;; =============================================================================
;; Signup

(defn- render-signup [req & {:keys [errors email first-name last-name]
                             :or   {errors     []
                                    email      ""
                                    first-name ""
                                    last-name  ""}}]
  (base (signup-content req errors email first-name last-name)))

(defn render [req]
  (ok (render-signup req)))

(defn- send-activation-email
  [user-id]
  (let [pattern [:account/email :account/first-name :account/last-name :account/activation-hash]
        {:keys [account/email
                account/first-name
                account/last-name
                account/activation-hash]} (account/query pattern user-id)]
    (send-email email "Starcity Account Activation"
                (format "Hello %s %s,\n%s/signup/activate?email=%s&hash=%s"
                        first-name
                        last-name
                        (:hostname config)
                        (url-encode email)
                        activation-hash))))

(defn signup! [{:keys [params] :as req}]
  (letfn [(-respond-malformed [& errors]
            (let [{:keys [first-name last-name email]} params]
              (malformed (render-signup req :errors errors
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

;; TODO: 'Resend' mechanism
(defn- render-invalid-activation
  [req]
  (base
   [:h2 "Your activation link is invalid or has expired."]))

;; =============================================================================
;; Activation

(defn activate! [{:keys [params session] :as req}]
  (let [{:keys [email hash]} params]
    (if (or (nil? email) (nil? hash))
      (render-invalid-activation req)
      (let [user (account/by-email email)]
        (if (= hash (:account/activation-hash user))
          (let [_       (account/activate! user)
                session (assoc session :identity (account/by-email email))]
            (-> (response/redirect +redirect-after-activation+)
                (assoc :session session)))
          ;; hashes don't match
          (ok (render-invalid-activation req)))))))
