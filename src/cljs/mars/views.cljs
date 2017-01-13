(ns mars.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [mars.menu.views :as m]
            [mars.activity.views :as activity]
            [mars.account.views :as account]
            [mars.components.antd :as a]
            [starcity.components.loading :as loading]))

(defn- pane [current-route]
  [:div.column.mars-pane.is-10
   {:style {:padding-bottom 0 :padding-left 0}}
   (case current-route
     :activity [activity/activity]
     :account  [account/account]
     :init     (loading/fill-container) ; handles the initial case
     [:section.section (str "TODO: Implement " current-route)])])

;; =============================================================================
;; API
;; =============================================================================

;; elements
;; - request room cleaning
;; - set up your profile
;; - time received for feed items

(defn app []
  (let [current-route (subscribe [:app/current-route])]
    (fn []
      [:div.columns.mars
       [m/menu]
       [pane @current-route]])))
