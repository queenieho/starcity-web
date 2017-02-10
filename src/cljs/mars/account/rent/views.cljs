(ns mars.account.rent.views
  (:require [mars.account.rent.components.upcoming :as upcoming]
            [mars.account.rent.components.bank-accounts :as bank-accounts]
            [mars.account.rent.link-account.views :as link-account]
            [mars.account.rent.history.views :as history]
            [mars.account.rent.components.security-deposit :as security-deposit]
            [mars.components.antd :as a]
            [mars.components.pane :as pane]
            [re-frame.core :refer [subscribe dispatch]]
            [cljs-time.core :as t]))

(defn- link-account-modal []
  (let [showing (subscribe [:rent/showing-link-account?])]
    (fn []
      [:div.modal {:class (when @showing "is-active")}
       [:div.modal-background
        {:on-click #(dispatch [:rent/toggle-link-account])}]
       [link-account/link-account]
       [:button.modal-close
        {:on-click #(dispatch [:rent/toggle-link-account])}]])))

(defn- enable-autopay-modal [showing enabling]
  [:div.modal {:class (when showing "is-active")}
   [:div.modal-background
    {:on-click #(dispatch [:rent/toggle-show-autopay])}]
   [:div.modal-content.box
    [:p.title.is-4 "What is Autopay?"]
    [:p "Autopay is a " [:b "rent subscription"] ". On the " [:b "1st"] " of
    every month, we'll automatically trigger an ACH payment for your rent,
    ensuring that you never miss a rent payment. When you move out, we'll also
    take care of turning it off."]
    ;; TODO: Note about when autopay will be starting
    [a/button
     {:style    {:margin-top "16px"}
      :type     "primary"
      :loading  enabling
      :on-click #(dispatch [:rent.autopay/enable])}
     "Enable"]]

   [:button.modal-close
    {:on-click #(dispatch [:rent/toggle-show-autopay])}]])

(defn- make-payment-modal [{:keys [amount id]} & {:keys [showing paying]}]
  (let [amount (if-not (integer? amount) (when amount (.toFixed amount 2)) amount)]
    [:div.modal {:class (when showing "is-active")}
     [:div.modal-background
      {:on-click #(dispatch [:rent.make-payment/toggle-show])}]
     [:div.modal-content.box
      [:p.title.is-4 "Make ACH Payment"]
      [:p "By pressing the " [:b "Pay Now"] " button below, you agree to pay "
       [:b "$" amount] " to Starcity using your bank account."]

      [:div {:style {:margin-top "16px"}}
       [a/button
        {:type     "primary"
         :loading  paying
         :on-click #(dispatch [:rent.make-payment/pay id])}
        "Pay Now"]]]
     [:button.modal-close
      {:on-click #(dispatch [:rent.make-payment/toggle-show])}]]))

(defn- content []
  (let [upcoming     (subscribe [:rent/upcoming])
        bank-account (subscribe [:rent/bank-account])
        autopay      (subscribe [:rent/autopay])
        make-payment (subscribe [:rent/make-payment])
        sec-deposit  (subscribe [:rent/security-deposit])]
    (fn []
      [:div
       [link-account-modal]
       [enable-autopay-modal (:enable-showing @autopay) (:enabling @autopay)]
       [make-payment-modal (:payment @make-payment)
        :showing (:showing @make-payment)
        :paying (:paying @make-payment)]
       [:div.columns
        [:div.column.is-one-third
         [:div {:style {:margin-bottom 16}}
          (let [{:keys [loading payment]} @upcoming]
            [upcoming/upcoming payment loading])]
         [:div {:style {:margin-bottom 16}}
          (let [{:keys [loading bank-account]} @bank-account]
            [bank-accounts/bank-accounts bank-account loading @autopay
             :on-link-account #(dispatch [:rent/toggle-link-account])
             :on-enable-autopay #(dispatch [:rent/toggle-show-autopay])])]
         [:div {:style {:margin-bottom 16}}
          [security-deposit/security-deposit
           @sec-deposit
           (:bank-account @bank-account)]]]
        [:div.column
         [history/history (:bank-account @bank-account)]]]])))

(defn view []
  [:div.rent
   (pane/pane
    (pane/header "Rent &amp; Security Deposit"
                 "Make rent payments, pay your security deposit &amp; manage autopay")
    (pane/content [content]))])
