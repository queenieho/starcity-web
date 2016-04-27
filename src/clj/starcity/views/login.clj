(ns starcity.views.login
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; Components

(defn- content [req]
  [:div.container
   [:form.form-signin
    [:h2.form-signin-heading "Please sign in"]
    [:label.sr-only {:for "inputEmail"} "Email address"]
    [:input#input-email.form-control
     {:type "email" :placeholder "Email address" :required true :autofocus true}]
    [:label.sr-only {:for "inputPassword"} "Password"]
    [:input#input-password.form-control
     {:type "password" :placeholder "Password" :required true}]
    [:button.btn.btn-lg.btn-primary.btn-block {:type "submit"} "Sign in"]]])

;; =============================================================================
;; API

(defn page [req]
  (base (content req) :css ["signin.css"]))
