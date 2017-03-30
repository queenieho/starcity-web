(ns onboarding.prompts.services.laundry
  (:require [ant-ui.core :as a]
            [onboarding.prompts.content :as content]
            [re-frame.core :refer [dispatch subscribe]]
            [toolbelt.core :as tb]))

(defn radio-group [keypath key value]
  [a/radio-group
   {:on-change #(dispatch [:prompt/update keypath key (= (.. % -target -value) "yes")])
    :value     (cond (true? value) "yes" (false? value) "no" :otherwise nil)}
   [a/radio {:value "yes"} "Yes"]
   [a/radio {:value "no"} "No"]])

(defn form
  [keypath {:keys [needed cleaning]}]
  [a/card
   [:div.control
    [:label.label "Would you like to sign up for laundry services?"]
    [radio-group keypath :needed needed]]
   (when needed
     [:div
      [:div.control
       [:label.label "Do you need dry cleaning?"]
       [radio-group keypath :cleaning cleaning]]
      [:div.control
       [:label.label "Do you need wash and fold?"]
       [radio-group keypath :cleaning cleaning]]])])

(defmethod content/content :services/laundry
  [{:keys [keypath data] :as item}]
  (let [{:keys [needed]} data]
    [:div.content
     [:p "Let us take care of keeping your shirts pressed and your jackets stain-free. Here are the details:"]
     [:ul
      [:li [:b "Next day service: "] "Request before 9am and we'll pick up your dirty laundry and return it clean the next day."]
      [:li "Use the provided laundry bag for wash and fold or grament bag for dry cleaning."]
      [:li "Hang the bags with dirty clothes on the hoook outside of your door."]
      [:li "Clean bags will be delivered the next day."]]
     [form keypath data]]))
