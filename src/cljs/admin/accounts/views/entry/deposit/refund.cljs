(ns admin.accounts.views.entry.deposit.refund
  (:require [re-frame.core :refer [dispatch subscribe]]
            [ant-ui.core :as a]
            [reagent.core :as r]
            [toolbelt.core :as tb]))


(defn footer [deposit]
  (let [submitting (subscribe [:deposit.refund/submitting?])
        can-submit (subscribe [:deposit.refund/can-submit?])]
    [:div
     [a/button {:size     :large
                :on-click #(dispatch [:deposit.refund/hide])}
      "Cancel"]
     [a/button {:size     :large
                :type     :primary
                :disabled (not @can-submit)
                :loading  @submitting
                :on-click #(dispatch [:deposit.refund/submit (:db/id deposit)])}
      "Refund"]]))


(defn form [deposit form-data]
  [:div
   [:div.field
    [:div.control
     [:label.label
      (str "How much would you like to refund of $" (:deposit/required deposit) "?")]
     [a/input-number
      {:value     (:amount form-data)
       :max       (:deposit/required deposit)
       :on-change #(dispatch [:deposit.refund.form/update :amount %])}]]]
   (when (< (:amount form-data) (:deposit/required deposit))
     [:div.field
      [:div.control
       [:label.label "Please explain why a full refund is not being issued."]
       [a/input {:type "textarea"
                 :rows 6
                 :on-change #(dispatch [:deposit.refund.form/update :deduction-reasons (.. % -target -value)])}]]])])


(defn modal [deposit]
  (let [showing   (subscribe [:deposit.refund/showing?])
        form-data (subscribe [:deposit.refund/form-data])]
    [a/modal {:visible   @showing
              :title     "Refund Security Deposit"
              :footer    (r/as-element [footer deposit])
              :on-cancel #(dispatch [:deposit.refund/hide])}
     [form deposit @form-data]]))
