(ns mars.account.rent.link-account.deposits.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mars.components.antd :as a]
            [reagent.core :as r]
            [starcity.dom :as dom]))

(defn- update! [key evt]
  (dispatch [:rent.link-account.deposits.bank-info/update key (dom/val evt)]))

(defn- country-select [country]
  (let [countries (subscribe [:rent.link-account.deposits/countries])]
    (fn [country]
      [:div.column.control
       [:label.label "Country"]
       [:span.select.is-fullwidth
        [:select
         {:required  true
          :value     country
          :on-change (partial update! :country)}
         (doall
          (for [{:keys [name code]} @countries]
            ^{:key code} [:option {:value code} name]))]]])))

(defn- currency-select [currency]
  (let [currencies (subscribe [:rent.link-account.deposits/currencies])]
    (fn [currency]
      [:div.column.control
       [:label.label "Currency"]
       [:span.select.is-fullwidth
        {:required  true
         :value     currency
         :on-change (partial update! :currency)}
        [:select
         (doall
          (for [c @currencies]
            ^{:key c} [:option {:value c} c]))]]])))

(defn- top-row [data]
  (let [currencies (subscribe [:rent.link-account.deposits/currencies])]
    (fn [data]
      (let [{:keys [account-holder country currency]} data]
        [:div.columns.control {:style {:margin-bottom 0}}
         [:div.column.control.is-half
          [:label.label "Account Holder's Name"]
          [:input.input
           {:type      "text"
            :required  true
            :value     account-holder
            :on-change (partial update! :account-holder)}]]
         [country-select country]
         [currency-select]]))))

(defn- bottom-row [data]
  (let [errors (subscribe [:rent.link-account.deposits.bank-info/errors])]
    (fn [data]
      (let [{:keys [routing-number account-number]} data]
        [:div.columns.control
         [:div.column.control
          [:label.label "Routing Number"]
          [:input.input
           {:required    true
            :placeholder "e.g. 110000000"
            :type        "number"
            :step        "1"
            :value       routing-number
            :on-change   (partial update! :routing-number)}]
          (when-let [e (:routing-number @errors)]
            [:span.help.is-danger e])]
         [:div.column.control
          [:label.label "Account Number"]
          [:input.input
           {:required    true
            :placeholder "e.g. 000123456789"
            :value       account-number
            :type        "number"
            :step        "1"
            :on-change   (partial update! :account-number)}]
          (when-let [e (:account-number @errors)]
            [:span.help.is-danger e])]]))))

(defn- collect-bank-info []
  (let [form-data  (subscribe [:rent.link-account.deposits.bank-info/form-data])
        can-submit (subscribe [:rent.link-account.deposits.bank-info/can-submit?])
        submitting (subscribe [:rent.link-account.deposits.bank-info/submitting?])]
    (r/create-class
     {:component-will-mount
      (fn [_]
        (dispatch [:app/load-scripts "https://js.stripe.com/v2/"]))
      :reagent-render
      (fn []
        [:div.content
         [:p "After you enter your bank account details below, we'll attempt to make "
          "two small deposits (microdeposits) in your account. "
          "This process will take " [:b "1-3 business days"] " to complete."]
         [:p "Please enter your bank details below:"]
         [:form {:on-submit #(do
                               (.preventDefault %)
                               (dispatch [:rent.link-account.deposits.bank-info/submit]))}
          ;; Name, Country, Currency
          [top-row @form-data]
          ;; Account number, Routing number
          [bottom-row @form-data]
          ;; TODO: Wrap in div w/ controls class
          [a/button {:html-type "button"
                     :type      "ghost"
                     :size      "large"
                     :style     {:margin-right "8px"}
                     :on-click  #(dispatch [:rent.link-account/choose-method])}
           [a/icon {:type "left"}]
           "Back"]
          [a/button {:html-type "submit"
                     :type      "primary"
                     :size      "large"
                     :disabled  (not @can-submit)
                     :loading   @submitting}
           (if @submitting "Submitting..." "Submit")]]])})))

(def ^:private verify-description
  [:div
   [:p "Your bank information has been successfully submitted, and the deposit process initiated."]
   [:p "This process usually takes "
    [:b "1-3 business days"]
    " to complete. Check your bank account over the coming few days, and "
    " look for two deposits with statement descriptions of "
    [:b "VERIFICATION."]]
   [:p "When you see the deposits, enter the amounts below in " [:em "cents."]]])

(defn- verify-form []
  (let [amounts (subscribe [:rent.link-account.deposits/amounts])]
    (fn []
      (let [[amount-1 amount-2] @amounts
            attrs               {:min 1 :step 1}]
        [:div.columns.control
         {:style {:margin-top "8px"}}
         [:div.column.control
          [:label.label "First deposit"]
          [:input.input
           (merge {:required    true
                   :type        "number"
                   :placeholder "e.g. 32"
                   :value       amount-1
                   :on-change   #(dispatch [:rent.link-account.deposits.amounts/update 0 (dom/val %)])}
                  attrs)]]
         [:div.column.control
          [:label.label "Second deposit"]
          [:input.input
           (merge {:required    true
                   :type        "number"
                   :placeholder "e.g. 45"
                   :value       amount-2
                   :on-change   #(dispatch [:rent.link-account.deposits.amounts/update 1 (dom/val %)])}
                  attrs)]]]))))

(defn verify-amounts []
  (let [can-submit  (subscribe [:rent.link-account.deposits.amounts/can-submit?])
        submitting? (subscribe [:rent.link-account.deposits.amounts/submitting?])]
    (fn []
      [:div.content
       [:form {:on-submit #(do
                             (.preventDefault %)
                             (dispatch [:rent.link-account.deposits/verify-amounts]))}
        verify-description
        [verify-form]
        [:div.controls
         [a/button {:type      "primary"
                    :size      "large"
                    :html-type "submit"
                    :loading   @submitting?
                    :disabled  (not @can-submit)}
          "Submit"]]]])))
