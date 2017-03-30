(ns onboarding.prompts.services.storage
  (:require [ant-ui.core :as a]
            [onboarding.prompts.content :as content]
            [re-frame.core :refer [dispatch subscribe]]
            [toolbelt.core :as tb]))

(defn- form
  [keypath {:keys [needed small large additional]}]
  [a/card
   [:div.control
    [:label.label "Will you need storage bins?"]
    [a/radio-group
     {:on-change #(dispatch [:prompt/update keypath :needed (= (.. % -target -value) "yes")])
      :value     (cond (true? needed) "yes" (false? needed) "no" :otherwise nil)}
     [a/radio {:value "yes"} "Yes"]
     [a/radio {:value "no"} "No"]]]
   (when needed
     [:div
      [:div.control
       [:label.label "Choose the number and type(s) of bins you'll need."]
       [:div.columns
        [:div.column.is-half
         [:div.level.is-mobile
          [:div.level-item
           [:i "18 gallon, $6/month"]]
          [:div.level-item
           [a/input-number
            {:min           0
             :max           10
             :step          1
             :default-value small
             :on-change     #(dispatch [:prompt/update keypath :small %])}]]]
         [:div.level.is-mobile
          [:div.level-item
           [:i "30 gallon, $8/month"]]
          [:div.level-item
           [a/input-number
            {:min           0
             :max           10
             :step          1
             :default-value large
             :on-change     #(dispatch [:prompt/update keypath :large %])}]]]]]]

      [:div.control
       [:label.label
        {:dangerouslySetInnerHTML
         {:__html "If you have any larger items that won't fit in a bin, we can probably work something out&mdash;just tell us about it below and we'll get back to you with a quote."}}]
       [a/input {:type          :textarea
                 :placeholder   "e.g. musical instruments, sports equipment, etc."
                 :default-value additional
                 :on-change     #(dispatch [:prompt/update keypath :additional (.. % -target -value)])}]]])])

(def ^:private description
  "Remove the clutter from your room and use our storage service. We'll provide
  you with bins that you can access anytime you'd like&mdash;just request the
  bins and we'll deliver them and pick them up the next day.")

(defmethod content/content :services/storage
  [{:keys [keypath data] :as item}]
  [:div.content
   [:p {:dangerouslySetInnerHTML {:__html description}}]
   [form keypath data]])
