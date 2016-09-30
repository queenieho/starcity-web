(ns starcity.views.components.footer
  (:require [hiccup.element :refer [link-to mail-to]]))

(def middot
  [:span {:style "margin: 0 4px;"} "&middot;"])

(def footer
  [:footer.footer
   [:div.container
    ;; TODO: Mobile layout
    [:div.columns.is-mobile
     [:div.column
      [:p
       (mail-to "team@joinstarcity.com")
       middot
       "415.496.9706"]]
     [:div.column.content.has-text-centered
      [:p "&copy; 2016 Starcity Properties, Inc."]]
     [:div.column.has-text-right
      [:p
       (link-to "/terms" "Terms of Service")
       middot
       (link-to "/privacy" "Privacy")]]]]])
