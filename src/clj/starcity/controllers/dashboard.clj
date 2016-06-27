(ns starcity.controllers.dashboard
  (:require [starcity.views.dashboard :as view]
            [starcity.controllers.utils :refer :all]))

;; =============================================================================
;; API
;; =============================================================================

(defn show-dashboard [req]
  (ok (view/dashboard)))
