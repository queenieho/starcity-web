(ns starcity.views.dashboard
  (:require [starcity.views.base :refer [base]]))

;; =============================================================================
;; API
;; =============================================================================

(defn dashboard
  []
  (base
   [:div.container
    [:div.page-header
     [:h1 "This is the dashboard"]]]))
