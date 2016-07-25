(ns starcity.views.application.community-fitness
  (:require [starcity.views.application.common :as common]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn textarea
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
    (textarea :placeholder "Dorms in college? Shared house in the city? Something else?"
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
              :placeholder "Tell us about your skills..."
              :required true
              :icon "mode_edit"
              :value answer)))

(defn- why-coliving-section
  [answer]
  (let [id "why-coliving"]
    (textarea :id id
              :name id
              :placeholder "Why Coliving?"
              :required true
              :icon "mode_edit"
              :value answer)))

;; =============================================================================
;; API
;; =============================================================================

(defn community-fitness
  [current-steps {:keys [why-coliving skills prior-community-housing]} & {:keys [errors]}]
  (let [sections [["Have you ever lived in community housing?"
                   (prior-community-housing-section prior-community-housing)]
                  ["What skills or traits do you hope to share with the community?"
                   (skills-section skills)]
                  ["Why are you interested in coliving?"
                   (why-coliving-section why-coliving)]]]
    (common/step "Community Fitness" sections current-steps :errors errors)))
