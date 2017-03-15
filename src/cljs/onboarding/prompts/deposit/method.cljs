(ns onboarding.prompts.deposit.method
  (:require [ant-ui.core :as a]
            [onboarding.prompts.content :as content]
            [re-frame.core :refer [dispatch subscribe]]))

(defmethod content/content :deposit/method
  [{:keys [keypath data] :as item}]
  (let [selected (:method data)]
    [:div.content
     [:p "There are two options available:"]
     [:ul
      [:li "You can pay us " [:strong "electronically"] " by using an ACH transfer from your bank account, or"]
      [:li "You can mail us a " [:strong "check"] "."]]

     [a/card
      [:div.control
       [:label.label "Choose your payment method:"]
       [a/radio-group
        {:on-change #(dispatch [:prompt/update keypath :method (.. % -target -value)])
         :value     selected}
        [a/radio {:value "ach"} "ACH"]
        [a/radio {:value "check"} "Check"]]]]]))
