(ns starcity.views.error
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; API
;; =============================================================================

(defn error
  ([message]
   (error "Whoops!" message))
  ([title message]
   (base
    [:div.container
     [:div.page-header
      [:h1.text-danger title]]
     [:p.text-danger.lead message]])))
