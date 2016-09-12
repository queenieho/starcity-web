(ns admin.views
  (:require [admin.routes :refer [build-path]]
            [admin.applications.views :refer [applications]]
            [admin.application.views :refer [application]]
            [re-frame.core :refer [subscribe dispatch]]
            [starcity.log :refer [log]]))

;; =============================================================================
;; Components
;; =============================================================================

(defn- active-tab
  [route tab-key]
  (= route tab-key))

(def ^:private panels
  [["Applications" :applications]])

(defn- nav-item
  [title key is-active?]
  [:a.nav-item.is-tab
   {:class (when is-active? "is-active")
    :href  (build-path (name key))}
   title])

(defn- navbar
  []
  (let [route (subscribe [:app/current-route])]
    (fn []
      [:nav.nav.has-shadow
       [:div.container
        [:div.nav-left
         [:a.nav-item.is-brand {:href "/admin"}
          [:span "Starcity"]]]
        [:div.nav-right
         (doall
          (for [[title key] panels]
            ^{:key key} [nav-item title key (= key @route)]))]]])))

(defn- home
  []
  [:main
   [:section.section
    [:div.container
     [:h1.title.is-1 "Home"]]]])

(defn- main
  []
  (let [route (subscribe [:app/current-route])]
    (fn []
      (case @route
        :applications [applications]
        :application  [application]
        [home]))))

;; =============================================================================
;; API
;; =============================================================================

(defn app
  []
  [:div
   [navbar]
   [main]])
