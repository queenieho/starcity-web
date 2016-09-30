(ns apply.community.views
  (:require [apply.prompts.views :as p]
            [apply.community.models :as m]
            [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as r]
            [starcity.dom :as dom]))

;; =============================================================================
;; Why Starcity
;; =============================================================================

(defn why-starcity []
  (let [answer (subscribe [:community.why-starcity/form-data])]
    (fn []
      (p/prompt
       (p/header "Why are you interested in joining Starcity?")
       (p/content
        [:div.content
         [:p "Tell us a bit about why you want to join one of our communities."]
         [:p "TODO: Indicate that this information will be available to the
       residents that are currently living in <communities>."]
         [:div.form-container
          [:div.form-group
           [:label.label "Enter your answer below."]
           [:textarea.textarea
            {:value     @answer
             :on-change #(dispatch [:community/why-starcity (dom/val %)])}]]]])))))

;; =============================================================================
;; About You
;; =============================================================================

(defn about-you []
  (let [answers (subscribe [:community.about-you/form-data])]
    (fn []
      (p/prompt
       (p/header "Tell us a bit about yourself.")
       (p/content
        [:div.content
         [:p "TODO:"]
         [:div.form-container
          [:div.form-group
           [:label.label "What do you like to do in your free time?"]
           [:textarea.textarea
            {:value     (:free-time @answers)
             :on-change #(dispatch [:community/about-you :free-time (dom/val %)])}]]
          [:div.form-group
           [:label.label "Do you have any dealbreakers?"]
           [:textarea.textarea
            {:value     (:dealbreakers @answers)
             :on-change #(dispatch [:community/about-you :dealbreakers (dom/val %)])}]]]])))))

;; =============================================================================
;; Communal Living
;; =============================================================================

(defn communal-living []
  (let [answers (subscribe [:community.communal-living/form-data])]
    (fn []
      (p/prompt
       (p/header "TODO:")
       (p/content
        [:div.content
         [:p "TODO:"]
         [:div.form-container
          [:div.form-group
           [:label.label "Do you have any experience with communal living?"]
           [:textarea.textarea
            {:value     (:prior-experience @answers)
             :on-change #(dispatch [:community/communal-living :prior-experience (dom/val %)])}]]
          [:div.form-group
           [:label.label "What will you bring to the community?"]
           [:textarea.textarea
            {:value     (:skills @answers)
             :on-change #(dispatch [:community/communal-living :skills (dom/val %)])}]]]])))))
