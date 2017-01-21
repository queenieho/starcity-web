(ns mars.account.rent.history.views
  (:require [mars.components.antd :as a]
            [re-frame.core :refer [subscribe dispatch]]
            [cljs-time.core :as t]
            [cljs.spec :as s]
            [cljs-time.format :as f]
            [reagent.core :as r]
            [starcity.components.icons :as i]
            [cljs-time.coerce :as c]
            [starcity.log :as l]))

(defn- first-day-of-month?
  [date-time]
  (= 1 (t/day date-time)))

(defn- last-day-of-month?
  [date-time]
  (= (t/day date-time) (t/day (t/last-day-of-the-month date-time))))

(defn- same-month?
  [dt1 dt2]
  (= (t/month dt1) (t/month dt2)))

(def ^:private short-formatter (f/formatter "M/d/yy"))

(def ^:private month-year-formatter
  (f/formatter "MMMM yyyy"))

(def ^:private month-day-year-formatter
  (f/formatter "MMMM dd, yyyy"))

(def ^:private month-day-formatter
  (f/formatter "MMMM dd"))

(defn- payment-date [start end]
  (if (and (first-day-of-month? start)
           (last-day-of-month? end)
           (same-month? start end))
    (f/unparse month-year-formatter start)
    (str
     (f/unparse month-day-formatter start)
     " &mdash; "
     (f/unparse month-day-year-formatter end))))

(s/fdef payment-date
        :args (s/cat :start t/date? :end t/date?)
        :ret string?)

(defn overdue? [status date]
  (and (= status "due")
       (t/after? (t/now) date)))

(defn late? [due-date paid-date]
  (t/after? (c/to-date-time paid-date) (c/to-date-time due-date)))

(defn- dot-props-for [status due-date]
  (case status
    "due"     [nil (when (overdue? status due-date) "red")]
    "pending" ["clock-circle-o"]
    "paid"    ["check-circle-o" "green"]))

(defn- payment-amount [{:keys [amount status due late-fee]}]
  (if late-fee
    [a/tooltip {:title "Because payment is past due, it has had a 10% late fee applied."}
     [:em amount]]
    [:span amount]))

(defn- payment-desc [{:keys [amount status due paid] :as item}]
  (let [paid? (= status "paid")]
    [:p
     "$"
     [payment-amount item]
     (if paid? " paid" " due")
     " on "
     (f/unparse short-formatter (if paid? paid due))]))

(def ^:private paid-or-pending? #{"paid" "pending"})

(defn- payment-tag [{:keys [method check desc] :as i}]
  [:span.tag.is-info.is-small
   (case method
     "check"   (str "check #" (:number check))
     "ach"     "ACH"
     "autopay" "autopay"
     "other"   desc)])

(defn- payment-button [{:keys [overdue] :as item} bank-account]
  (let [button [a/button
                {:size     "small"
                 :type     "ghost"
                 :on-click #(dispatch [:rent/make-payment item])
                 :disabled (not bank-account)}
                "Pay Now"]]
    [:div
     (if bank-account
       button
       [a/tooltip {:title "Link your bank account to make payments."}
        button])
     (when overdue
       [:span.tag.is-danger.is-small
        {:style {:margin-left "8px"}}
        "overdue"])]))

(defn- payment-item
  [{:keys [id status method pstart pend due paid] :as item} bank-account last]
  (let [payment-date (payment-date pstart pend)
        [type color] (dot-props-for status due)]
    [a/timeline-item
     {:dot   (when type (r/as-element [a/icon {:type type}]))
      :last  last
      :color (or color "blue")}
     [:p.heading [:strong {:dangerouslySetInnerHTML {:__html payment-date}}]]
     (payment-desc item)
     [:div {:style {:margin-top "4px"}}
      (if (paid-or-pending? status)
        (payment-tag item)
        [payment-button item bank-account])
      (when (and (= status "paid") (late? due paid))
        [:span.tag.is-danger.is-small {:style {:margin-left 8}} "late"])]]))

(defn history [bank-account]
  (let [loading (subscribe [:rent.history/loading?])
        items   (subscribe [:rent.history/items])]
    (fn [bank-account]
      [a/card {:title   "Rent Payments"
               :loading @loading}
       [a/timeline
        (let [items    @items
              last-idx (dec (count items))]
          (doall
           (map-indexed
            (fn [idx item]
              ^{:key (:id item)} [payment-item item bank-account (= idx last-idx)])
            items)))]])))
