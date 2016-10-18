(ns admin.core
  (:require [admin.routes :as routes]
            [admin.views :refer [app]]
            [admin.events]
            [admin.subs]
            [starcity.log :refer [log]]
            [reagent.core :as reagent]
            [re-frame.core :refer [dispatch-sync]]))

;; =============================================================================
;; Config
;; =============================================================================

(enable-console-print!)

;; =============================================================================
;; API
;; =============================================================================

(defn ^:export run
  []
  (routes/app-routes)
  (dispatch-sync [:app/initialize])
  (reagent/render [app] (.getElementById js/document "app")))
