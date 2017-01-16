(ns mars.account.rent.link-account.authorize.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]
            [mars.components.antd :as a]
            [cljs-time.core :as t]
            [starcity.dom :as dom]
            [starcity.log :as l]))

(def ^:private date-formatter (f/formatter "MMMM d, yyyy"))

(defn- tooltipped [content tooltip-content]
  [:span.tooltip
   [a/tooltip
    {:title tooltip-content}
    [:span content]]])

(defn- ending-tooltip-data [license-term end-date]
  (if (= license-term 1)
    [" until move-out."
     "This is because you chose a month-to-month term."]
    [(str " until " (f/unparse date-formatter end-date) ".")
     "Unless you renew your membership."]))

(defn- ending [license-term end-date]
  (let [[text tooltip] (ending-tooltip-data license-term end-date)]
    [tooltipped text tooltip]))

(defn charge-amount [amount]
  [:span
   "I understand that I will be charged "
   [tooltipped
    (str "$" amount)
    "This is your monthly rent."]])

(defn at-time []
  [:span
   " on the "
   [tooltipped
    "1st"
    "We always bill on the first to keep things simple."]
   " of every month starting on "])

(defn starting [start-date]
  [:span
   (when start-date
     [tooltipped
      (f/unparse date-formatter start-date)
      "This is the first full month that we will bill you for using autopay."])
   " and continuing "])

(defn- acknowledgement
  [acknowledged rent-amount start-date license-term end-date]
  [:div.acknowledgement
   [a/checkbox {:on-change #(do
                              (.preventDefault %)
                              (dispatch [:rent.link-account.authorize.acknowledge/toggle]))
                :checked   acknowledged}
    [:span.prompt
     (charge-amount rent-amount)
     (at-time)
     (starting start-date)
     (ending license-term end-date)]]])

(defn authorize-autopay []
  (let [plan-data    (subscribe [:rent.link-account.authorize/plan])
        acknowledged (subscribe [:rent.link-account.authorize/acknowledged?])
        subscribing  (subscribe [:rent.link-account.authorize/subscribing?])]
    (fn []
      (let [{:keys [rent-amount start-date
                    license-term move-in end-date]} @plan-data]
        [:div.authorize.content
         [:p "Great! Your bank account has been linked and verified."]
         [:p "Please review the details of your payment plan:"]
         [acknowledgement
          @acknowledged
          rent-amount
          start-date
          license-term
          end-date]
         [:div.controls
          [a/button {:type     "primary"
                     :size     "large"
                     :on-click #(dispatch [:rent.link-account.authorize/submit])
                     :disabled (not @acknowledged)
                     :loading  @subscribing}
           "Finish"]]]))))
