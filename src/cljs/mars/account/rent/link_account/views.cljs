(ns mars.account.rent.link-account.views
  (:require [mars.account.rent.link-account.deposits.views :as deposits]
            [mars.account.rent.link-account.authorize.views :as authorize]
            [mars.components.antd :as a]
            [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]
            [starcity.components.icons :as i]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]))

(defn choose-verification []
  (let [plaid-loading (subscribe [:rent.link-account.plaid/loading?])]
    (r/create-class
     {:component-will-mount
      (fn [_]
        (dispatch [:app/load-scripts "https://cdn.plaid.com/link/v2/stable/link-initialize.js"]))
      :reagent-render
      (fn []
        [:div.content
         [:p "We use ACH for rent payments, which requires you to link a bank account."]
         [:p "In order to link your bank account you'll need to verify that you own it, which can be done in one of the following ways:"]
         [:ul
          [:li [:p "You can use your " [:b "bank credentials"] " to verify ownership " [:em "instantly,"]]]
          [:li [:p "or you can enter your bank details " [:b "manually"] ", we'll make two microdeposits, "
                "and then you'll verify the amounts. This will take " [:b "2-3 business days."]]]]

         [:p "Which would you prefer?"]
         [a/button {:type     "primary"
                    :on-click #(dispatch [:rent.link-account/plaid])
                    :size     "large"
                    :loading  @plaid-loading
                    :style    {:margin-right "8px"}}
          "Use Bank Credentials"]
         [a/button {:type     "ghost"
                    :size     "large"
                    :on-click #(dispatch [:rent.link-account/deposits])
                    :disabled @plaid-loading}
          "Microdeposits"]])})))

(def ^:private error-view-message
  "Something appears to have gone wrong. If refreshing the page doesn't work, please contact us.")

(defn- error-view []
  [:div.has-text-centered {:style {:width "80%"
                                   :margin-right "auto"
                                   :margin-left "auto"
                                   :margin-bottom "24px"}}
   [:div (i/error :large)]
   [:p.title.is-5 error-view-message]])

(defn- title [status]
  (case status
    :bank-needed "Enter Bank Account Details"
    :unverified  "Verify Microdeposits"
    :error       ""
    "Choose Verification Method"))

(defn link-account []
  (let [status  (subscribe [:rent.link-account/status])
        loading (subscribe [:rent.link-account/loading?])]
    (fn []
      [:div.modal-content.box
       [:p.title.is-5 [:b (title @status)]]
       (case @status
         :bank-needed [deposits/collect-bank-info]
         :unverified  [deposits/verify-amounts]
         :error       (error-view)
         [choose-verification])])))
