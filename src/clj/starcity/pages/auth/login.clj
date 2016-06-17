(ns starcity.pages.auth.login
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [malformed ok]]
            [starcity.pages.auth.common :refer :all]
            [starcity.models.account :as account]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.string :refer [trim lower-case]]
            [ring.util.response :as response]))

;; =============================================================================
;; Constants

(def +redirect-after-login+ "/me")

;; =============================================================================
;; Components

(defn- content
  [req errors email next-url]
  [:div.container
   [:form.form-signin {:action "/login" :method "post"}
    [:h2.form-signin-heading "Please sign in"]
    (for [e errors]
      [:div.alert.alert-danger {:role "alert"} e])
    [:input {:type "hidden" :name "next" :value next-url}]
    [:label.sr-only {:for "inputEmail"} "Email address"]
    [:input#input-email.form-control
     {:name        "email"
      :type        "email"
      :placeholder "Email address"
      :required    true
      :autofocus   (when (= email "") true)
      :value       email}]
    [:div.form-group
     [:label.sr-only {:for "inputPassword"} "Password"]
     [:input#input-password.form-control
      {:name "password" :type "password" :placeholder "Password" :required true
       :autofocus (when (not= email "") true)}]]
    [:button.btn.btn-lg.btn-primary.btn-block {:type "submit"} "Sign in"]]])

;; =============================================================================
;; Authentication

(defn- validate-credentials
  [credentials]
  (b/validate
   credentials
   {:email    [(required "An email address is required.")
               [v/email :message "That email address is invalid."]]
    :password [(required "A password is required.")
               [v/min-count 8 :message "Your password should be at least 8 characters long."]]}))

(defn- clean-credentials
  [credentials]
  (-> (update credentials :email (comp trim lower-case))
      (update :password trim)))

;; =============================================================================
;; API

(defn- render* [{:keys [identity] :as req}
                & {:keys [errors email] :or {errors [] email ""}}]
  ;; NOTE: Preserves the next url through the POST req by using a hidden input
  (let [next-url (get-in req [:params :next])]
    (base (content req errors email next-url) :css ["signin.css"])))

(defn render
  "Render the landing page."
  [req]
  (ok (render* req)))

(defn- url-after-login [acct {:keys [params] :as req}]
  (cond
    (not-empty (:next params)) (:next params)
    (account/applicant? acct)  "/application"
    :otherwise                 +redirect-after-login+))

(defn login!
  "Log a user in."
  [{:keys [params session] :as req}]
  (let [vresult (-> params clean-credentials validate-credentials)]
    (if-let [{:keys [email password]} (valid? vresult)]
      (if-let [acct (account/authenticate email password)]
        (if (:account/activated acct)
          ;; success
          (let [next-url (url-after-login acct req)
                session  (assoc session :identity acct)]
            (-> (response/redirect next-url)
                (assoc :session session)))
          ;; account not activated
          (malformed (render* req :errors ["Please click the activation link in your inbox before attempting to log in."]
                             :email email)))
        ;; authentication failure
        (malformed (render* req
                           :errors ["The credentials you entered are invalid; please try again."]
                           :email email))) ; TODO: Need more here?
      ;; validation failure
      (malformed (render* req :errors (errors-from vresult) :email (:email params))))))
