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
    :content
    [:main
     [:div.container
      [:h1.red-text title]
      [:div.divider]
      [:p.red-text.flow-text message]]])))
