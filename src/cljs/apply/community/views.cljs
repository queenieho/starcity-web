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
  (let [saved-answer (subscribe [:community/why-starcity])
        answer       (r/atom @saved-answer)]
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
            {:default-value @answer
             :on-change     #(reset! answer (dom/val %))}]]]])
       (p/footer
        :previous-prompt :personal/income
        :next [p/next-button
               :on-click #(dispatch [:prompt/next @answer])
               :disabled (not (m/answer-long-enough? @answer))])))))

;; =============================================================================
;; About You
;; =============================================================================

(defn about-you []
  (let [saved-answers (subscribe [:community/about-you])
        answer        (r/atom @saved-answers)]
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
            {:default-value (:free-time @answer)
             :on-change     #(swap! answer assoc :free-time (dom/val %))}]]
          [:div.form-group
           [:label.label "Do you have any dealbreakers?"]
           [:textarea.textarea
            {:default-value (:dealbreakers @answer)
             :on-change     #(swap! answer assoc :dealbreakers (dom/val %))}]]]])
       (p/footer
        :previous-prompt :community/why-starcity
        :next [p/next-button
               :on-click #(dispatch [:prompt/next @answer])
               :disabled (not (m/about-you-complete? @answer))])))))

;; =============================================================================
;; Communal Living
;; =============================================================================

(defn communal-living []
  (let [saved-answers (subscribe [:community/communal-living])
        answer        (r/atom @saved-answers)]
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
            {:default-value (:prior-experience @answer)
             :on-change     #(swap! answer assoc :prior-experience (dom/val %))}]]
          [:div.form-group
           [:label.label "What will you bring to the community?"]
           [:textarea.textarea
            {:default-value (:skills @answer)
             :on-change     #(swap! answer assoc :skills (dom/val %))}]]]])
       (p/footer
        :previous-prompt :community/about-you
        :next [p/next-button
               :on-click #(dispatch [:prompt/next @answer])
               :disabled (not (m/communal-living-complete? @answer))])))))
