(ns admin.home.views
  (:require [admin.components.content :as c]
            [admin.components.header :as h]
            [admin.content :refer [app-content]]
            [admin.util :as u]
            [ant-ui.core :as a]
            [re-frame.core :refer [dispatch subscribe]]
            [toolbelt.core :as tb]))

(defn- date-val [data k]
  (when-let [d (get data k)] (u/date->input-format d)))

(defn- on-change [k]
  #(dispatch [:home.metrics.controls/update k (.. % -target -value)]))

(defn account-controls []
  (let [controls (subscribe [:home.metrics/controls])]
    [a/card {:title "Controls"}
     [:p "Choose the date range with which to calculate metrics. The default is to use the current calendar month."]
     [:form
      {:on-submit #(do
                     (.preventDefault %)
                     (dispatch [:home.metrics/update! @controls]))
       :style     {:margin-top 16}}

      [:div.control
         [:label.label "From"]
         [a/input {:type      "date"
                   :class     "ant-input"
                   :value     (date-val @controls :from)
                   :on-change (on-change :from)}]]

      [:div.control
         [:label.label "To"]
         [a/input {:type      "date"
                   :class     "ant-input"
                   :value     (date-val @controls :to)
                   :on-change (on-change :to)}]]

      [:div.control
       [a/button
        {:type      :primary
         :html-type :submit}
        "Update"]]]]))

(defn account-metrics []
  (let [loading (subscribe [:home.metrics/loading?])
        metrics (subscribe [:home/metrics])]
    (println @metrics)
    [a/card {:loading @loading}
     [:div.level
      [:div.level-item.has-text-centered
       [:p.heading "Accounts Created"]
       [:p.subtitle.is-1 (:accounts/created @metrics)]]
      [:div.level-item.has-text-centered
       [:p.heading "Applications Created"]
       [:p.subtitle.is-1 (:applications/created @metrics)]]]]))

(defn metrics []
  [:div
   [:div.columns
    [:div.column [account-metrics]]
    [:div.column.is-one-quarter [account-controls]]]])

(defn content []
  [c/content [metrics]])

(defmethod app-content :home [_]
  [a/layout
   (h/header)
   [content]])
