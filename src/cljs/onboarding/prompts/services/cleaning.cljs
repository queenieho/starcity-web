(ns onboarding.prompts.services.cleaning
  (:require [ant-ui.core :as a]
            [onboarding.prompts.content :as content]
            [re-frame.core :refer [dispatch subscribe]]
            [toolbelt.core :as tb]))

(defmethod content/content :services/cleaning
  [{:keys [keypath data] :as item}]
  (let [{:keys [needed]} data]
    [:div.content
     [:p "Starcity's weekly cleaning service includes:"]
     [:ul
      [:li "Laundering and replacing your bedding linens"]
      [:li "Dusting"]
      [:li "Vacuuming"]
      [:li "Cleaning of surfaces"]]
     [:p "You can have your room cleaned in a one-off capacity for " [:b "$40 each time"]
      " (in your member dashboard), or sign up for a weekly subscription of " [:b "$125/month"] "."]
     [a/card
      [:div.control
       [:label.label "Would you like to sign up for weekly cleaning now?"]
       [a/radio-group
        {:on-change #(dispatch [:prompt/update keypath :needed (= (.. % -target -value) "yes")])
         :value     (cond (true? needed) "yes" (false? needed) "no" :otherwise nil)}
        [a/radio {:value "yes"} "Yes"]
        [a/radio {:value "no"} "No"]]]]]))
