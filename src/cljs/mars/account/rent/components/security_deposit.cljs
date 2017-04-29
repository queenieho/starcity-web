(ns mars.account.rent.components.security-deposit
  (:require [re-frame.core :refer [dispatch]]
            [mars.components.antd :as a]
            [starcity.components.icons :as i]
            [reagent.core :as r]
            [cljs-time.format :as f]))

(def error-msg
  "Something went wrong. Are you connected to the internet?")

(def ^:private date-formatter (f/formatter "M/d/yy"))

(def ^:private icon-style
  {:font-size "42px" :margin-bottom "8px"})

(def error-view
  [:div.has-text-centered
   [:p.is-danger
    [a/icon {:type "close-circle" :style icon-style}]]
   [:p error-msg]])

(defn- paid? [{:keys [received required]}]
  (>= received required))

(defn- pending? [{pending :pending}]
  (> pending 0))

(defmulti content
  (fn [sd _ _]
    (cond
      (paid? sd)    :paid
      (pending? sd) :pending
      :otherwise    :unpaid)))

(defmethod content :paid [{:keys [required]} _ _]
  [:div.has-text-centered
   [:p;.is-success
    [a/icon {:type "check-circle" :style icon-style}]]
   [:p.subtitle.is-4 (str "$" required " paid")]])

(defmethod content :pending [{:keys [received pending due-by]}]
  [:div
   [:div.columns
    [:div.column.has-text-centered
     [:p.heading "Paid"]
     [:p.subtitle.is-5 (str "$" received)]]
    [:div.column.has-text-centered
     [:p.heading "Pending"]
     [:p.subtitle.is-5 (str "$" pending)]]
    [:div.column.has-text-centered
     [:p.heading "Due By"]
     [:p.subtitle.is-5 (f/unparse date-formatter due-by)]]]])

(defn payment-button [disabled]
  [a/button {:type     "primary"
             :on-click #(dispatch [:rent.security-deposit/show-confirmation])
             :disabled disabled}
   "Make Payment"])

(defn- payment-amount [{:keys [received required]}]
  (- required received))

(defmethod content :unpaid
  [{:keys [due-by] :as sd} bank-linked]
  [:div
   [:div.columns
    [:div.column.has-text-centered
     [:p.heading "Amount Due"]
     [:p.subtitle.is-5 (str "$" (payment-amount sd))]]
    (when due-by
      [:div.column.has-text-centered
       [:p.heading "Due By"]
       [:p.subtitle.is-5 (f/unparse date-formatter due-by)]])]

   [:div.has-text-centered
    (if-not bank-linked
      [a/tooltip {:title "You'll need to link a bank account first!"}
       (payment-button (not bank-linked))]
      (payment-button (not bank-linked)))]])

(defn- last-four [number]
  [:span
   {:dangerouslySetInnerHTML
    {:__html (str "&middot;&middot;&middot;&middot; " number)}}])

(defn- confirmation-modal
  [showing bank-account security-deposit paying]
  [:div.modal {:class (when showing "is-active")}
   [:div.modal-background
    {:on-click #(dispatch [:rent.security-deposit/hide-confirmation])}]
   [:div.modal-content.box
    [:p.title.is-4 "Confirm Payment"]
    [:p "By pressing the " [:b "Pay Now"] " button below, you are agreeing to pay "
     [:b "$" (payment-amount security-deposit)] " to Starcity using "
     " your linked bank account: "
     [:b (:bank-name bank-account) " " (last-four (:number bank-account))] "."]

    [:div {:style {:margin-top "16px"}}
     [a/button
      {:type     "primary"
       :loading  paying
       :on-click #(dispatch [:rent.security-deposit/pay])}
      "Pay Now"]]]
   [:button.modal-close
    {:on-click #(dispatch [:rent.security-deposit/hide-confirmation])}]])

(defn security-deposit
  [{:keys [loading error bank-linked security-deposit paying confirmation]}
   bank-account]
  [:div
   [confirmation-modal confirmation bank-account security-deposit paying]
   [a/card {:title "Security Deposit" :loading loading}
    (if error
      error-view
      (content security-deposit bank-linked paying))]])
