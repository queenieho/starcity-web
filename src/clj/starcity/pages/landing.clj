(ns starcity.pages.landing
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]))

;; =============================================================================
;; Constants


;; =============================================================================
;; Components

(defn- landing-content []
  [:div.site-wrapper
   [:div.site-wrapper-inner
    [:div.cover-container
     [:div.masthead.clearfix
      [:div.inner
       [:h3.masthead-brand "Starcity"]
       [:nav
        [:ul.nav.masthead-nav
         [:li.active [:a {:href "/"} "Home"]]
         [:li [:a {:href "#About"} "About"]]
         [:li [:a {:href "#contact"} "Contact"]]]]]]

     [:div.inner.cover
      [:h1.cover-heading "Starcity"]
      [:p.lead "Coming Soon."]
      [:p.lead
       [:a.btn.btn-lg.btn-default {:href "#"} "Learn more"]]]

     [:div.mastfoot
      [:div.inner
       [:p "Yep."]]]]]])

(defn- landing-view []
  (base (landing-content) :css ["cover.css"]))

;; =============================================================================
;; API

(defn handle [req]
  (ok (landing-view)))
