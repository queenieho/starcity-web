(ns starcity.pages.auth.login
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [malformed]]
            [starcity.pages.auth.common :refer :all]
            [starcity.models.account :as account]
            [starcity.middleware :refer [get-component]]
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

(defn render [req & {:keys [errors email] :or {errors [] email ""}}]
  ;; NOTE: Preserves the next url through the POST req by using a hidden input
  (let [next-url (get-in req [:params :next] +redirect-after-login+)]
    (base (content req errors email next-url) :css ["signin.css"])))

(defn authenticate
  "Authenticate the user by checking email and password."
  [{:keys [params session] :as req}]
  (let [db      (get-component req :db)
        vresult (-> params clean-credentials validate-credentials)]
    (if-let [{:keys [email password]} (valid? vresult)]
      (if-let [user (account/authenticate db email password)]
        ;; success
        (let [next-url (get-in req [:params :next])
              session  (assoc session :identity user)]
          (-> (response/redirect next-url)
              (assoc :session session)))
        ;; authentication failure
        (malformed (render req
                           :errors ["The credentials you entered are invalid; please try again."]
                           :email email))) ; TODO: Need more here?
      ;; validation failure
      (malformed (render req :errors (errors-from vresult) :email (:email params))))))
