(ns starcity.controllers.application
  (:require [starcity.views.application :as view]
            [starcity.controllers.utils :refer :all]))

;; =============================================================================
;; API
;; =============================================================================

(defn show-application
  [req]
  (ok (view/application)))
