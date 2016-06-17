(ns starcity.pages.landing
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]
            [hiccup.core :refer [html]]))

;; =============================================================================
;; Constants

;; =============================================================================
;; Components

(def navbar
  [:nav.navbar
   [:div.container
    [:div.navbar-header
     ;; [:button.navbar-toggle.collapsed
     ;;  {:type "button" :data-toggle "collapse" :data-target "#navbar"
     ;;   :aria-expanded "false" :aria-controls "navbar"}
     ;;  [:span.sr-only "Toggle navigation"]
     ;;  [:span.icon-bar]
     ;;  [:span.icon-bar]
     ;;  [:span.icon-bar]]
     [:a.navbar-brand {:href "#"}
      [:img {:alt "Starcity" :src "/assets/img/starcity-brand-icon-white.png"}]]]
    ;; [:div#navbar.collapse.navbar-collapse
    ;;  [:ul.nav.navbar-nav]]
    ]])

(defn- landing-content []
  [:div.navbar-wrapper
   [:div.container-fluid
    navbar

    [:div.header
     [:div.header-inner
      [:div.header-content
       [:div.container
        [:h1 "reimagine home in San Francisco"]
        [:p.lead "Help shape community-focused housing for our city's workforce."]
        [:button.btn.btn-lg.btn-primary "Get Involved"]]]]]

    [:div.container.marketing

     [:div.row.featurette
      [:div.col-md-7
       [:h2.featurette-heading
        "Diverse, Resource-Efficient Communities. "
        [:span.text-muted ""]]
       [:p.lead "Hard-working San Franciscans form the diverse fabric of this city. Yet we're often left out of the housing conversation. Let's change that. Let's build housing that allows us to thrive in our beloved city. Let's build communities that embrace individuals from all walks of life."]]
      [:div.col-md-5
       [:img.featurette-image.img-responsive.img-rounded
        {:src "/assets/img/renderings/alpharendering.png"}]]]

     [:hr.featurette-divider]
     [:div.row.featurette
      [:div.col-md-5
       [:img.featurette-image.img-responsive.img-rounded
        {:src "/assets/img/renderings/sf_rendering04.png"}]]
      [:div.col-md-7
       [:h2.featurette-heading
        "Beautiful Private Spaces. <br> "
        [:span.text-muted "For Everyone."]]
       [:p.lead "Balancing community space and resources with adequate private space will honor the needs of our workforce. Let's design private rooms in a way that'll allow us to live sustainably in San Francisco."]]]

     [:hr#informed-divider.featurette-divider]

     [:div#informed-section.row
      [:div.col-md-6.col-md-offset-3
       [:h2#informed-heading "Get Involved"]
       [:p.lead "Enter your email and we'll be in touch."]

       [:form {:action "/register" :method "GET"}
        [:div.input-group.input-group-lg
         [:input.input.form-control
          {:type        "email"
           :name        "email"
           :required    true
           :placeholder "Enter your email address"}]
         [:span.input-group-btn
          [:button.btn.btn-primary "Join Us"]]]]]]

     [:hr.featurette-divider]

     [:footer
      [:p.pull-right [:a {:href "#"} "Back to top"]]
      [:p "&copy; 2016 Starcity Properties, Inc."]]]]])

(defn- landing-view []
  (base (landing-content) :css ["landing.css"]))

;; =============================================================================
;; API

(defn render [req]
  (ok (landing-view)))
