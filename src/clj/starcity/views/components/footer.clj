(ns starcity.views.components.footer
  (:require [starcity.views.components.layout :as l]
            [hiccup.core :refer [html]]
            [hiccup.element :refer [link-to mail-to]]))

(def middot
  [:span {:style "margin: 0 4px;"} "&middot;"])

(def ^:private non-mobile
  (l/columns
   {:class "is-hidden-mobile"}
   (l/column
    [:p.heading "Links"]
    [:ul
     [:li (link-to "/about" "About Us")]
     [:li (link-to "/team" "Team")]])
   (l/column
    {:class "has-text-centered"}
    [:p {:style "margin-top: 20px;"}
     (link-to "/terms" "Terms of Service")
     middot
     (link-to "/privacy" "Privacy")]
    [:p "&copy; 2016 Starcity Properties, Inc."])
   (l/column
    {:class "has-text-right"}
    [:p.heading "Contact"]
    [:ul
     [:li (mail-to "team@joinstarcity.com")]
     [:li "415.496.9706"]])))

(def ^:private mobile
  (l/columns
   {:class "is-hidden-desktop is-hidden-tablet has-text-centered"}
   (l/column
    [:p
     (link-to "/about" "About Us")
     middot
     (link-to "/team" "Team")])
   (l/column
    [:p
     (mail-to "team@joinstarcity.com")
     middot
     "415.496.9706"])
   (l/column
    [:p
     (link-to "/terms" "Terms of Service")
     middot
     (link-to "/privacy" "Privacy")]
    [:p "&copy; 2016 Starcity Properties, Inc."])))

(def footer
  (html
   [:footer.footer
    (l/container
     mobile
     non-mobile)]))

;; [:div.container
;;     ;; TODO: Mobile layout
;;     [:div.columns.has-text-centered;.is-mobile
;;      [:div.column
;;       ]
;;      [:div.column.content;.has-text-centered
;;       ]
;;      [:div.column;.has-text-right
;;       ]]
;;     ]
