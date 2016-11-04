(ns admin.account.entry.security-deposit.views
  (:require [re-frame.core :refer [dispatch subscribe]]
            [clojure.string :as s]
            [starcity.log :as l]
            [starcity.components.loading :as loading]
            [cljs-time.coerce :as c]
            [cljs-time.format :as f]
            [cljs-time.core :as t]
            [starcity.components.icons :as i]
            [reagent.core :as r]
            [starcity.dom :as dom]))

(defn- title []
  (let [status (subscribe [:account.entry.security-deposit/status])]
    (fn []
      [:p.title.is-3
       "Security Deposit is " [:b (case @status
                                    :partial "partially paid"
                                    (name @status))]])))

(defn- overview []
  (let [security-deposit (subscribe [:account.entry.security-deposit/overview])]
    (fn []
      [:nav.level.is-mobile
       [:div.level-item.has-text-centered
        [:p.heading "Amount Received"]
        [:p (str "$" (:amount-received @security-deposit))]]
       [:div.level-item.has-text-centered
        [:p.heading "Amount Required"]
        [:p (str "$" (:amount-required @security-deposit))]]
       [:div.level-item.has-text-centered
        [:p.heading "Payment Method"]
        [:p (s/upper-case (or (:payment-method @security-deposit) "n/a"))]]
       [:div.level-item.has-text-centered
        [:p.heading "Payment Type"]
        [:p (s/upper-case (or (:payment-type @security-deposit) "n/a"))]]])))

(def date-formatter
  (f/formatter "h:mma, M-d-yyyy"))

(defn- charge [{:keys [url status created amount] :as c}]
  (let [created (f/unparse date-formatter (c/from-long (* created 1000)))]
    [:div.box
     [:nav.level
      [:div.level-item
       [:p
        (case status
          "pending"   (i/hourglass)
          "succeeded" (i/check-circle)
          "failed"    (i/cross-circle)
          (i/question-circle))
        [:strong status] " payment of " [:strong (str "$" (float (/ amount 100)))]]]
      [:div.level-item.has-text-centered
       [:p.heading "View Details"]
       [:p [:a {:href url :target "_blank"} (i/stripe)]]]
      [:div.level-item.has-text-centered
       [:p.heading "Created At"]
       [:small [:b created]]]]]))

(defn- check [{:keys [name bank number amount date received-on status] :as c}]
  [:div.box
   {:on-click #(dispatch [:account.entry.security-deposit.check/show-modal c])
    :style    {:cursor "pointer"}}
   [:nav.level.has-text-centered.is-mobile
    [:div.level-item
     [:p.heading "Name"]
     [:b name]]
    [:div.level-item
     [:p.heading "Check #"]
     [:p number]]
    [:div.level-item
     [:p.heading "Amount"]
     [:p (str "$" amount)]]
    [:div.level-item
     [:p.heading "Status"]
     [:em status]]
    [:div.level-item
     [:p.heading "Date"]
     [:em date]]
    [:div.level-item
     [:p.heading "Received On"]
     [:em received-on]]]])

(defn- transactions []
  (let [charges (subscribe [:account.entry.security-deposit/charges])
        checks  (subscribe [:account.entry.security-deposit/checks])]
    (fn []
      [:div
       (when-not (empty? @charges)
         [:div
          [:h3.subtitle.is-4 "Stripe Charges"]
          (doall
           (for [c @charges]
             ^{:key (:id c)} [charge c]))])
       (when-not (empty? @checks)
         [:div
          [:h3.subtitle.is-4 "Checks"]
          (doall
           (for [c @checks]
             ^{:key (:id c)} [check c]))])])))

(defn- footer []
  [:footer.card-footer
   [:a.card-footer-item
    {:on-click #(dispatch [:account.entry.security-deposit.check/show-modal])}
    "Add Check"]])

(defn- check-modal []
  (let [check-statuses (subscribe [:account.entry.security-deposit.check/statuses])
        showing        (subscribe [:account.entry.security-deposit.check/showing-modal])
        data           (subscribe [:account.entry.security-deposit.check/modal-data])]
    (fn []
      [:div.modal {:class (when @showing "is-active")}
       [:div.modal-background
        {:on-click #(dispatch [:account.entry.security-deposit.check/hide-modal])}]
       [:div.modal-content.box
        [:p.title.is-4 "Add a Check"]
        [:form {:on-submit #(do
                              (.preventDefault %)
                              (dispatch [:account.entry.security-deposit/save-check @data]))}
         ;; Name & bank
         [:div.control.is-grouped
          [:p.control.is-expanded
           [:label.label "Name on check"]
           [:input.input
            {:type      "text"
             :value     (:name @data)
             :required  true
             :on-change #(dispatch [:account.entry.security-deposit.check/update :name (dom/val %)])}]]
          [:p.control.is-expanded
           [:label.label "Bank (optional)"]
           [:input.input
            {:type        "text"
             :value       (:bank @data)
             :placeholder "e.g. Wells Fargo"
             :on-change   #(dispatch [:account.entry.security-deposit.check/update :bank (dom/val %)])}]]]

         ;; Amount
         [:p.control
          [:label.label "Amount (dollars)"]
          [:input.input
           {:type        "number"
            :required    true
            :value       (:amount @data)
            :on-change   #(dispatch [:account.entry.security-deposit.check/update :amount (js/parseFloat (dom/val %))])
            :placeholder "e.g. 500"}]]

         ;; Number & Status
         [:div.columns
          [:div.column
           [:p.control.is-expanded
            [:label.label "Check number"]
            [:input.input
             {:type        "number"
              :step        1
              :required    true
              :value       (:number @data)
              :on-change   #(dispatch [:account.entry.security-deposit.check/update :number (js/parseInt (dom/val %))])
              :placeholder "e.g. 4174"}]]]
          [:div.column
           [:p.control.is-expanded
            [:label.label "Status"]
            [:span.select.is-fullwidth
             [:select {:value     (or (:status @data) "choose")
                       :on-change #(dispatch [:account.entry.security-deposit.check/update :status (dom/val %)])}
              [:option {:disabled true :value "choose"} "Choose"]
              (for [status @check-statuses]
                ^{:key status} [:option {:value status} status])]]]]]

         ;; Date on check & date received
         [:div.control.is-grouped
          [:p.control.is-expanded
           [:label.label "Date on check"]
           [:input.input
            {:type      "date"
             :required  true
             :value     (:date @data)
             :on-change #(dispatch [:account.entry.security-deposit.check/update :date (dom/val %)])}]]
          [:p.control.is-expanded
           [:label.label "Date received"]
           [:input.input
            {:type      "date"
             :required  true
             :value     (:received-on @data)
             :on-change #(dispatch [:account.entry.security-deposit.check/update :received-on (dom/val %)])}]]]

         [:p.control
          [:button.button.is-primary {:type "submit"} "Save"]]]]
       [:button.modal-close
        {:type     "button"
         :on-click #(dispatch [:account.entry.security-deposit.check/hide-modal])}]])))

(defn view []
  (let [loading (subscribe [:account.entry.security-deposit/loading?])]
    (fn []
      [:div
       [check-modal]
       (if @loading
         (loading/container "fetching security deposit")
         [:div.card.is-fullwidth
          [:div.card-content
           [title]
           [:hr]
           [overview]
           [transactions]]
          [footer]])])))
