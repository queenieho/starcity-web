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
   [:div.container
    [:div.page-header
     [:h1 (format "Thank you for getting involved, <b>%s</b>!" email)]]
    [:div
     [:p.lead "With your help, we'll design and build community-focused housing
      that will keep San Francisco's diversity right here, in our city. "]
     [:p.lead "Let's work together to make San Francisco a financially attainable
      place to live by adding new housing supply to our city. Be on the lookout
      for an email with more information on how to participate."]]]))
