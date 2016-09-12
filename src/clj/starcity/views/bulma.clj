(ns starcity.views.bulma
  (:require [starcity.views.base :as b]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-css include-js]]
            [hiccup.element :refer [link-to mail-to unordered-list]]
            [cheshire.core :as json]))
(def navbar
  [:nav.nav
   [:div.container
    [:div.nav-left
     [:a.nav-item.is-brand {:href "/"}
      [:span "Starcity Logo"]]]]])

(def middot
  [:span {:style "margin: 0 4px;"} "&middot;"])

(def footer
  [:footer.footer
   [:div.container
    [:div.columns.is-mobile
     [:div.column
      [:p.title.is-5 "Company"]
      [:ul
       [:li (link-to "/about" "About")]
       [:li (link-to "/team" "Team")]
       [:li (link-to "/blog" "Blog")]]]
     [:div.column.has-text-right
      [:p.title.is-5 "Contact"]
      [:ul
       [:li (mail-to "team@joinstarcity.com")]
       [:li "415.496.9706"]]]]
    [:div.content.has-text-centered
     [:p
      "&copy; 2016 Starcity Properties, Inc."]
     [:p
      (link-to "/terms" "Terms of Service")
      middot
      (link-to "/privacy" "Privacy")]]]])

(defn- gradient-background-image
  [url]
  {:style (format "background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('%s');" url) :class "has-background-image"})

(defn base
  "Page template with a solid navbar."
  [& {:keys [content js json title req]
      :or   {js [], json []}}]
  (html5
   {:lang "en"}
   (b/head (if title (str "Starcity &mdash; " title) "Starcity"))
   [:body
    navbar
    content
    footer
    (apply include-js (concat b/base-js js))
    (for [[name obj] json]
      [:script
       (format "var %s = %s" name (json/encode obj))])
    (include-js
     "/assets/bower/jquery-validation/dist/jquery.validate.js"
     "/js/main.js")
    b/google-analytics]))

(def hero
  [:section.hero.is-fullheight.is-primary
   [:div.hero-body
    (gradient-background-image "/assets/img/starcity-kitchen.png")
    [:div.container
     [:h1.title.is-1 "Your new home."]
     [:h2.subtitle.is-4
      "Comfortable, communal housing in "
      [:strong "San Francisco"]]]]])

(def basic-content
  [:main
   hero])

(defn bulma
  [req]
  (base :title "Bulma Playground"
        :content basic-content))
