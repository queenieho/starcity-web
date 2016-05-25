(ns starcity.pages.landing
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]))

;; =============================================================================
;; Constants

;; =============================================================================
;; Components

(defn- landing-content []
  [:div.page-container
   [:div.navbar.navbar-static-top
    [:div.navbar-inner
     [:div.container
      [:a.brand {:href "/"}
       [:img {:alt "Starcity" :src "/assets/img/starcity-logo-icon.png"}]
       [:a#login_btn.btn.btn-navbar {:href "/login"} "Sign in"]
       ]]]]
   [:div#page.container
    [:div.homepage
     [:div.introsection
      [:div.introsignedout
       [:div.introcontent
        [:div.introslogan
         [:h1 "Reimagining Urban Living"]
         [:h2 "What does home mean to you?"]
         [:p "Help shape community-centric housing in San Francisco."]]
        [:div.form-container 
         [:form {:action "#" :method "GET"}
          [:input.email-field {:type "text" :name "email" :id "user_email" :placeholder "Enter your email address"}]
          [:input.submit-btn {:type "submit" :value "Join us"}]]]]]]]]])

(defn- landing-view []
  (base (landing-content) :css ["landing.css"] :body-class "starcity-page-landing"))

;; =============================================================================
;; API

(defn handle [req]
  (ok (landing-view)))
