(ns starcity.views.auth.signup
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; Helpers
;; =============================================================================



(defn- form-group
  [id label attrs]
  [:div.form-group
   [:label.control-label.col-sm-2 {:for id} label]
   [:div.col-sm-10
    [:input.form-control (merge {:id id} attrs)]]])

;; =============================================================================
;; API
;; =============================================================================

(defn signup
  "The content for the signup page."
  [errors email first-name last-name]
  (base
   [:div.container
    [:div.row
     [:form.col-xs-8.col-xs-offset-2.form-horizontal {:action "/signup" :method "post"}
      [:h2.text-center "Sign Up"]
      (for [e errors]
        [:div.alert.alert-danger {:role "alert"} e])

      ;; TODO: iterate over data spec
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

      (form-group "input-email" "Email"
                  {:name        "email"
                   :type        "email"
                   :placeholder "Email address"
                   :required    true
                   :autofocus   (when (empty? email) true)
                   :value       email})

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
        [:button.btn.btn-success {:type "submit"} "Create Account"]]]]]]
   :css ["signup.css"]))

(defn invalid-activation
  []
  (base [:h2 "Your activation link is invalid or has expired."]))

(defn signup-complete
  []
  (base [:h2 "Check your inbox, yo."]))
