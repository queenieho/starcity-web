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
       [:img {:alt "Starcity" :src "/assets/img/starcity-logo.png"}]
       [:a#login_btn.btn.btn-navbar {:href "/login"} "Log in"]]]]]])

(defn- landing-view []
  (base (landing-content) :css ["landing.css"] :body-class "starcity-page-landing"))

;; =============================================================================
;; API

(defn handle [req]
  (ok (landing-view)))
