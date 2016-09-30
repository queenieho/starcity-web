(ns apply.views
  (:require [apply.menu.views :refer [menu]]
            [apply.overview.views :as overview]
            [apply.logistics.views :as logistics]
            [apply.personal.views :as personal]
            [apply.community.views :as community]
            [re-frame.core :refer [subscribe dispatch]]
            [starcity.components.notifications :as n]))

;; =============================================================================
;; Components
;; =============================================================================

;; NOTE: Currently assuming they're all errors...
(defn- notification [idx {:keys [message]}]
  [n/danger message #(dispatch [:notification/delete idx])])

(defn- prompt []
  (let [current-prompt (subscribe [:prompt/current])]
    (fn []
      (case @current-prompt
        :overview/welcome          [overview/welcome]
        :overview/advisor          [overview/advisor]
        :logistics/communities     [logistics/choose-communities]
        :logistics/license         [logistics/choose-license]
        :logistics/move-in-date    [logistics/move-in-date]
        :logistics/pets            [logistics/pets]
        :personal/phone-number     [personal/phone-number]
        :personal/background       [personal/background-check-info]
        :personal/income           [personal/income-verification]
        :community/why-starcity    [community/why-starcity]
        :community/about-you       [community/about-you]
        :community/communal-living [community/communal-living]
        [:div.content (str "TODO: Implement view for " @current-prompt)]))))

;; =============================================================================
;; API
;; =============================================================================

(defn app []
  (let [notifications (subscribe [:app/notifications])]
    (fn []
      [:div.container
       [:div.columns
        [:div.column.is-one-quarter
         [menu]]
        [:div.column
         (doall
          (map-indexed
           (fn [idx n]
             ^{:key (str "notification-" idx)} [notification idx n])
           @notifications))
         [prompt]]]])))
