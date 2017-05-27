(ns admin.home.views
  (:require [admin.components.content :as c]
            [admin.components.header :as h]
            [admin.content :refer [app-content]]
            [admin.util :as u]
            [ant-ui.core :as a]
            [re-frame.core :refer [dispatch subscribe]]
            [admin.components.table :as tbl]
            [toolbelt.core :as tb]
            [reagent.core :as r]
            [admin.routes :as routes]))

;; =============================================================================
;; Controls
;; =============================================================================

(defn- date-val [data k]
  (when-let [d (get data k)] (u/date->input-format d)))

(defn- on-change [k]
  #(dispatch [:home.controls/update k %]))

(defn account-controls []
  (let [controls (subscribe [:home/controls])]
    [a/card {:title "Controls"}
     [:p "Choose the date range with which to calculate metrics. The default is to use the current calendar month."]
     [:form
      {:on-submit #(do
                     (.preventDefault %)
                     (dispatch [:home.metrics/update! @controls]))
       :style     {:margin-top 16}}

      [:div.field
       [:label.label "From"]
       [a/date-picker
        {:value       (:from @controls)
         :allow-clear false
         :format      "MM/DD/YYYY"
         :on-change   (on-change :from)}]]

      [:div.field
       [:label.label "To"]
       [a/date-picker
        {:value       (:to @controls)
         :allow-clear false
         :format      "MM/DD/YYYY"
         :on-change   (on-change :to)}]]

      [:div.field
       [a/button
        {:type      :primary
         :html-type :submit}
        "Update"]]]]))

;; =============================================================================
;; Accounts
;; =============================================================================

(defn account-metrics []
  (let [loading (subscribe [:home.metrics/loading?])
        metrics (subscribe [:home/metrics])]
    [a/card {:loading @loading}
     [:div.columns
      [:div.column.has-text-centered
       [:p.heading "Accounts Created"]
       [:p.subtitle.is-1 (:accounts/created @metrics)]]
      [:div.column.has-text-centered
       [:p.heading "Applications Created"]
       [:p.subtitle.is-1 (:applications/created @metrics)]]]]))

;; =============================================================================
;; Referrals
;; =============================================================================

(def referral-columns
  [(tbl/column "source" :title "Referral Source")
   (tbl/column "from" :render #(r/as-element [:span (name (:from %2))]))
   (tbl/column "account"
               :render
               #(r/as-element
                 (if (some? %)
                   [:a {:href (routes/path-for :account :account-id (:id %))} (:name %)]
                   "N/A")))
   (tbl/column "email"
               :render
               #(let [account (:account %2)]
                  (if (some? account)
                    (r/as-element [:a {:href (routes/path-for :account :account-id (:id account))} (:email account)])
                    "N/A")))
   (tbl/column "created"
               :render
               #(r/as-element (.format (js/moment. %) "MM/DD/YY")))])

(defn referral-footer [referrals]
  (let [stats (->> (group-by :source referrals)
                   (reduce (fn [acc [k v]] (assoc acc k (count v))) {}))]
    [:div
     (for [[source num] stats]
       ^{:key source} [:span {:style {:margin-right 6}} [:b source ":"] num])]))

(defn referrals []
  (let [loading   (subscribe [:home.referrals/loading?])
        referrals (subscribe [:home/referrals])]
    [a/card {:loading @loading
             :title   "Referrals"}
     [a/table {:dataSource (clj->js @referrals)
               :size       :small
               :columns    referral-columns
               :footer     #(r/as-element (referral-footer @referrals))}]]))

;; =============================================================================
;; Wrapper
;; =============================================================================

(defn metrics []
  [:div
   [:div.columns
    [:div.column
     [account-metrics]
     [:div {:style {:margin-top 16}}
      [referrals]]]
    [:div.column.is-one-quarter [account-controls]]]])

(defn content []
  [c/content [metrics]])

(defmethod app-content :home [_]
  [a/layout
   (h/header)
   [content]])
