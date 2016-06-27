(ns starcity.views.availability
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; API
;; =============================================================================

(defn availability
  [units-available]
  (base
   [:div.container
    [:div.page-header
     [:h1 "52 Gilbert Street"]]
    ;; TODO: Don't hard-code this
    [:p.lead (format "Units available: %s" units-available)]
    [:a {:href "/application"} "Apply Now"]]))
