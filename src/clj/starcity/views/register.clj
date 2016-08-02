(ns starcity.views.register
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; Helpers
;; =============================================================================


;; =============================================================================
;; API
;; =============================================================================

(defn registration
  [email]
  (base
   :content
   [:main
    [:div.container
     [:h3 (format "Thank you for getting involved, <b>%s</b>!" email)]
     [:div.divider]
     [:div
      [:p.flow-text "With your help, we'll design and build community-focused housing
      that will enable more San Franciscans to stay in our city. "]
      [:p.flow-text "Let's work together to make San Francisco an attainable
      place to live by creating welcoming communities the embrace the city's diversity and culture. Be on the lookout
      for an email with more information on how to participate."]]]]))
