(ns starcity.views.faq
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; API
;; =============================================================================

(defn faq
  []
  (base
   [:div.container
    [:div.page-header
     [:h1 "FAQ"]]
    [:p "FACK!"]]))
