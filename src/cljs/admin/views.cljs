(ns admin.views
  (:require [admin.routes :refer [build-path]]
            [admin.application.list.views :refer [applications]]
            [admin.application.entry.views :refer [application]]
            [admin.notify.views :refer [notifications]]
            [re-frame.core :refer [subscribe dispatch]]
            [starcity.log :refer [log]]
            [starcity.log :as l]))

;; =============================================================================
;; Components
;; =============================================================================

(defn- active-tab
  [route tab-key]
  (= route tab-key))

(def ^:private panels
  [["Applications" :application/list]])

(def ^:private panel-urls
  {:application/list "applications"})

(defn- nav-item
  [title key is-active?]
  [:a.nav-item.is-tab
   {:class (when is-active? "is-active")
    :href  (build-path (get panel-urls key))}
   title])

(defn- navbar []
  (let [route (subscribe [:app/current-route])]
    (fn []
      [:header.nav.has-shadow
       [:div.container
        [:div.nav-left
         [:a.nav-item.is-brand {:href "/admin"}
          [:img {:src "/assets/img/starcity-logo-black.png"}]
          [:span "Starcity"]]]
        [:div.nav-right
         (doall
          (for [[title key] panels]
            ^{:key key} [nav-item title key (= key @route)]))
         [:a.nav-item.is-tab {:href "/logout"} "Log Out"]]]])))

(defn- home []
  [:main
   [:section.section
    [:div.container
     [:h1.title.is-1 "Welcome, admin!"]]]])

(defn- main []
  (let [route (subscribe [:app/current-route])]
    (fn []
      [:section.section {:style {:min-height "100vh"}}
       (case @route
         :application/list  [applications]
         :application/entry [application]
         [home])])))

;; =============================================================================
;; API
;; =============================================================================

(defn app
  []
  [:div
   [navbar]
   [notifications]
   [main]])
