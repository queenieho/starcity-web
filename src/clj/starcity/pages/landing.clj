(ns starcity.pages.landing
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]
            [starcity.router :refer [route]]
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
        [:h1 "reimagine the urban home"]
        [:p.lead "Help us shape community-focused housing for San Francisco's workforce."]
        [:button.btn.btn-lg.btn-primary "Learn More"]]]]]

    [:div.container.marketing

     [:div.row.featurette
      [:div.col-md-7
       [:h2.featurette-heading
        "First Heading "
        [:span.text-muted "It'll blow your mind."]]
       [:p.lead "Donec sed odio dui. Etiam porta sem malesuada magna mollis euismod. Nullam id dolor id nibh ultricies vehicula ut id elit. Morbi leo risus, porta ac consectetur ac, vestibulum at eros. Praesent commodo cursus magna."]]
      [:div.col-md-5
       [:img.featurette-image.img-responsive.img-rounded
        {:src "/assets/img/renderings/sf_rendering04.png"}]]]

     [:hr.featurette-divider]
     [:div.row.featurette
      [:div.col-md-5
       [:img.featurette-image.img-responsive.img-rounded
        {:src "/assets/img/renderings/sf_rendering04.png"}]]
      [:div.col-md-7
       [:h2.featurette-heading
        "Second Heading "
        [:span.text-muted "It'll blow your mind."]]
       [:p.lead "Donec sed odio dui. Etiam porta sem malesuada magna mollis euismod. Nullam id dolor id nibh ultricies vehicula ut id elit. Morbi leo risus, porta ac consectetur ac, vestibulum at eros. Praesent commodo cursus magna."]]]

     [:hr#informed-divider.featurette-divider]

     [:div#informed-section.row
      [:div.col-md-6.col-md-offset-3
       [:h2#informed-heading "Stay Informed"]
       [:p.lead "Enter your email below to receive updates."]

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

(defmethod route [:index :get] [_ _]
  (ok (landing-view)))
