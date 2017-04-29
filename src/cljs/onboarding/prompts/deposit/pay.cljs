(ns onboarding.prompts.deposit.pay
  (:require [ant-ui.core :as a]
            [onboarding.prompts.content :as content]
            [re-frame.core :refer [dispatch subscribe]]))

(defn- options [full-amount]
  [:ul
   [:li
    [:strong "Partial deposit: "]
    "$500 prior to move-in, and the rest "
    [:em "at the end of your first month."]]
   [:li [:strong "Full deposit: "] (str "Your entire deposit ($" full-amount ") prior to move-in.")]])

(defn- ach
  [{:keys [keypath data] :as item}]
  (let [full-amount (subscribe [:deposit.pay/amount])]
    [:div
     [:p "Now that your account is verified we can accept your payment! You can pay either:"]
     (options @full-amount)

     [a/card
      [:div.control
       [:label.label "How much would you like to pay now?"]
       [a/radio-group
        {:on-change #(dispatch [:prompt/update keypath :method (.. % -target -value)])
         :value     (:method data)}
        [a/radio {:value "partial"} [:b "$500"] " (partial)"]
        [a/radio {:value "full"} [:b "$" @full-amount] " (full)"]]]]]))

(defn- address [llc]
  [:div
   [:p
    [:span llc]
    [:br]
    [:span "1020 Kearny St."]
    [:br]
    [:span "San Francisco, CA 94133"]]])

(defn- check
  [{:keys [keypath data] :as item}]
  (let [full-amount (subscribe [:deposit.pay/amount])
        llc         (subscribe [:deposit.pay/llc])]
    [:div
     [:p "Please give your check to your community manager " [:i "OR"] " mail it to the following address:"]
     [:blockquote (address @llc)]
     (options @full-amount)
     [:p [:em "This step will be marked complete after we have received your check."]]]))

(defn- content [item]
  (let [method (subscribe [:deposit/payment-method])]
    (if (= @method "ach")
      [ach item]
      [check item])))

(defmethod content/content :deposit/pay [item]
  [:div.content [content item]])
