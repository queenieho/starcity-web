(ns mars.views
  (:require [re-frame.core :refer [subscribe]]))

;; =============================================================================
;; Components
;; =============================================================================

(defn- navbar []
  (let [route (subscribe [:app/current-route])]
    (fn []
      [:header.nav.has-shadow
       [:div.container
        [:div.nav-left
         [:a.nav-item.is-brand {:href "/me"}
          [:img {:src "/assets/img/starcity-logo-black.png"}]
          [:span "Starcity"]]]
        [:div.nav-right
         ;; (doall
         ;;  (for [[title key] panels]
         ;;    ^{:key key} [nav-item title key (= key @route)]))
         [:a.nav-item.is-tab {:href "/logout"} "Log Out"]]]])))

;; =============================================================================
;; API
;; =============================================================================

(defn app []
  [:div
   [navbar]
   [:section.section {:style {:min-height "100vh"}}]])
