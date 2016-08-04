(ns starcity.views.application.community-fitness
  (:require [starcity.views.application.common :as common]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- textarea
  [& {:keys [label value icon] :as attrs}]
  [:div.input-field
   (when icon
     [:i.material-icons.prefix icon])
   [:textarea.materialize-textarea
    (dissoc attrs :value :icon :label)
    value]
   [:label {:for (:id attrs)} label]])

(defn- prior-community-housing-section
  [answer]
  (let [id "prior-community-housing"]
    (textarea :placeholder "Dorms in college? Shared house in the city? Please describe your experiences."
              :id id
              :name id
              :required true
              :icon "mode_edit"
              :value answer)))

(defn- skills-section
  [answer]
  (let [id "skills"]
    (textarea :id id
              :name id
              :placeholder "Our members love to learn from one-another; what can you teach them?"
              :required true
              :icon "mode_edit"
              :value answer)))

(defn- why-interested-section
  [answer]
  (let [id "why-interested"]
    (textarea :id id
              :name id
              :placeholder "What excites you most about our community?"
              :required true
              :icon "mode_edit"
              :value answer)))

(defn- free-time-section
  [answer]
  (let [id "free-time"]
    (textarea :id id
              :name id
              :placeholder "Tell us about any hobbies, non-work commitments, or other activites in which you partake."
              :required true
              :icon "mode_edit"
              :value answer)))

(defn- dealbreakers-section
  [answer]
  (let [id "dealbreakers"]
    (textarea :id id
              :name id
              :placeholder "e.g. Can't stand cats? Have a major food allergy?"
              :icon "mode_edit"
              :value answer)))

;; =============================================================================
;; API
;; =============================================================================

(defn community-fitness
  [current-steps
   {:keys [why-interested skills prior-community-housing dealbreakers free-time]}
   & {:keys [errors]}]
  (let [sections (map (partial apply common/make-step)
                      [["Have you ever lived in community housing?"
                        (prior-community-housing-section prior-community-housing)]
                       ["What skills or traits do you hope to share with the community?"
                        (skills-section skills)]
                       ["Why are you interested in Starcity?"
                        (why-interested-section why-interested)]
                       ["How do you spend your free time?"
                        (free-time-section free-time)]
                       ["Do you have any dealbreakers?"
                        (dealbreakers-section dealbreakers)]])]
    (common/step "Community Fitness" sections current-steps :errors errors)))
