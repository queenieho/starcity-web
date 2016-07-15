(ns starcity.views.terms
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; API
;; =============================================================================

(defn terms
  []
  (base
   [:div.container
    [:div.page-header
     [:h1 "Terms of Service"]
     [:p.lead "Coming Soon"]]]))
