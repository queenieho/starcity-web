(ns starcity.views.application.community-fitness
  (:require [starcity.views.application.common :as common]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- prior-community-housing-section
  [answer]
  (let [id "prior-community-housing"]
    [:div.form-group
     [:label.sr-only {:for id} "Prior community housing experience"]
    [:textarea.form-control
     {:id          id
      :name        id
      :value       answer
      :required    true
      :placeholder "Tell us about it..."}]]))

(defn- skills-section
  [answer]
  (let [id "skills"]
    [:div.form-group
     [:label.sr-only {:for id} "Skills"]
     [:textarea.form-control
      {:id          id
       :name        id
       :value       answer
       :required    true
       :placeholder "Tell us about your skills..."}]]))

(defn- why-coliving-section
  [answer]
  (let [id "why-coliving"]
    [:div.form-group
     [:label.sr-only {:for id} "Why Coliving?"]
     [:textarea.form-control
      {:id          id
       :name        id
       :value       answer
       :required    true
       :placeholder "Tell us about why you want to live here..."}]]))

;; =============================================================================
;; API
;; =============================================================================

(defn community-fitness
  [current-steps]
  (let [sections [["Have you ever lived in community housing?"
                   (prior-community-housing-section "")]
                  ["What skills or traits do you hope to share with the community?"
                   (skills-section "")]
                  ["Why are you interested in coliving?"
                   (why-coliving-section "")]
                  ["Which community activities/services would you be interested in participating in?"
                   [:div "TODO:"]]]]
    (common/application
     current-steps
     [:div.question-container
      [:form {:method "POST"}
       [:ul.question-list
        (for [[title content] sections]
          (common/section title content))]
       common/onward]]
     :title "Community Fitness"
     :js ["bower/jquery-validation/dist/jquery.validate.js"
          "validation-defaults.js"
          ;; TODO: community.js
          ])))
