(ns onboarding.prompts.deposit.pay
  (:require [ant-ui.core :as a]
            [onboarding.prompts.content :as content]
            [re-frame.core :refer [dispatch subscribe]]))

(defn- options []
  [:ul
   [:li
    [:strong "Partial deposit: "]
    "$500 prior to move-in, and the rest "
    [:em "at the end of your first month."]]
   [:li [:strong "Full deposit: "] "Your entire deposit prior to move-in."]])

(defn- ach
  [{:keys [keypath data] :as item}]
  (let [amount (subscribe [:deposit.pay/amount])]
    [:div
     [:p "Now that your account is verified we can accept your payment! You can pay either:"]
     (options)

     [a/card
      [:div.control
       [:label.label "How much would you like to pay now?"]
       [a/radio-group
        {:on-change #(dispatch [:prompt/update keypath :method (.. % -target -value)])
         :value     (:method data)}
        [a/radio {:value "partial"} [:b "$500"] " (partial)"]
        [a/radio {:value "full"} [:b "$" @amount] " (full)"]]]]]))

(defn- check
  [{:keys [keypath data] :as item}]
  [:div
   [:p "test test"]
   ])

(defn- content [item]
  (let [method (subscribe [:deposit/payment-method])]
    (if (= @method "ach")
      [ach item]
      [check item])))

(defmethod content/content :deposit/pay [item]
  [:div.content [content item]])
