(ns starcity.controllers.team
  (:require [starcity.views.team :as view]
            [ring.util.response :as response]
            [starcity.controllers.utils :refer :all]))

;; =============================================================================
;; API
;; =============================================================================

(defn show-team
  [req]
  (ok (view/team req)))
