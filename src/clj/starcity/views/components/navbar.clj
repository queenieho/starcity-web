(ns starcity.views.components.navbar
  (:require [hiccup.element :refer [link-to]]
            [hiccup.def :refer [defelem]]))

(defn- brand [& [inverse?]]
  (let [logo-url (if inverse?
                   "/assets/img/starcity-brand-icon-white.png"
                   "/assets/img/starcity-logo-black.png")]
    [:a.nav-item.is-brand {:href "/"}
     [:img {:src logo-url}]
     [:span {:class (when inverse? "is-inverse")} "Starcity"]]))

(defn nav-item
  [uri content & [button?]]
  (if button?
    [:span.nav-item
     (link-to {:class "button is-primary"} uri content)]
    (link-to {:class "nav-item is-tab"} uri content)))

(defn nav-button
  [uri content]
  (nav-item uri content true))

(defn navbar [inverse? & nav-items]
  [:header.nav
   {:class (when-not inverse? "has-shadow")}
   [:div.container
    [:div.nav-left (brand inverse?)]
    [:span#nav-toggle.nav-toggle
     (for [_ (range 3)] [:span])]
    [:div.nav-right.nav-menu nav-items]]])
