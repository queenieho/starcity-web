(ns starcity.pages.auth.signup
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [malformed]]
            [starcity.pages.auth.common :refer :all]
            [starcity.models.account :as account]
            [starcity.middleware :refer [get-component]]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.string :refer [trim capitalize lower-case]]
            [ring.util.response :as response]))

;; =============================================================================
;; Components

(defn- content
  [req errors email first-name last-name]
  [:div.container
   [:form.form-signin {:action "/signup" :method "post"}
    [:h2.form-signin-heading "Sign Up"]
    (for [e errors]
      [:div.alert.alert-danger {:role "alert"} e])

    [:label.sr-only {:for "inputEmail"} "Email address"]
    [:input.form-control
     {:name        "email"
      :type        "email"
      :placeholder "Email address"
      :required    true
      :autofocus   (when (empty? email) true)
      :value       email}]

    [:div.form-group
     [:label.sr-only {:for "inputFirstName"} "First Name"]
     [:input.form-control
      {:name        "first-name"
       :type        "text"
       :placeholder "First Name"
       :required    true
       :value       first-name}]]

    [:div.form-group
     [:label.sr-only {:for "inputLastName"} "Last Name"]
     [:input.form-control
      {:name        "last-name"
       :type        "text"
       :placeholder "Last Name"
       :required    true
       :value       last-name}]]

    [:div.form-group
     [:label.sr-only {:for "inputPassword"} "Password"]
     [:input.form-control
      {:name        "password"
       :type        "password"
       :placeholder "Password"
       :required    true}]]

    [:button.btn.btn-lg.btn-primary.btn-block {:type "submit"} "Sign in"]]])

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

;; =============================================================================
;; API

(defn render-complete [req]
  (base
   [:h3 "Check your inbox, yo."]))

(defn render [req & {:keys [errors email first-name last-name]
                     :or   {errors     []
                            email      ""
                            first-name ""
                            last-name  ""}}]
  (base (content req errors email first-name last-name) :css ["signin.css"]))

(defn signup
  ""
  [{:keys [params session] :as req}]
  (let [db      (get-component req :db)
        vresult (-> params clean-params validate)]
    (if-let [{:keys [email password first-name last-name]} (valid? vresult)]
      (if-not (account/exists? db email)
        ;; success
        (let [user    (account/create! db email password first-name last-name)
              session (assoc session :identity user)]
          (do
            ;; TODO: SEND EMAIL
            (response/redirect "/signup/complete")))
        ;; account already exists for email
        (malformed (render req :errors [(format "An account is already registered for %s." email)]
                           :first-name first-name
                           :last-name last-name)))
      ;; validation failure
      (malformed (render req :errors (errors-from vresult)
                         :email (:email params)
                         :first-name (:first-name params)
                         :last-name (:last-name params))))))
