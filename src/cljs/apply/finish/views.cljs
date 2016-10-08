(ns apply.finish.views
  (:require [apply.prompts.views :as p]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]))

(defn- payment-content []
  (let [agreed (subscribe [:finish.pay.terms/agreed?])]
    (fn []
      [:div.content
       [:p "TODO:"]
       [:div.form-container
        [:div.form-group
         [:label.label "Have you read our Terms of Service and Privacy Policy?"]
         [:p.control
          [:label.checkbox
           [:input.checkbox {:type      "checkbox"
                             :checked   @agreed
                             :on-change #(dispatch [:finish.pay/toggle-agree])}]
           "Yes " [:span {:dangerouslySetInnerHTML {:__html "&mdash;"}}]
           " I have read and agree to both."]]]]])))

(defn pay []
  (p/prompt
   (p/header "Great! Almost done!")
   (p/content [payment-content])))
