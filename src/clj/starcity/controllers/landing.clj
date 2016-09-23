(ns starcity.controllers.landing
  (:require [starcity.views.bulma.landing :as view]
            [starcity.controllers.utils :refer :all]))

;; =============================================================================
;; API
;; =============================================================================

(defn show-landing
  [req]
  (ok (view/landing req)))
