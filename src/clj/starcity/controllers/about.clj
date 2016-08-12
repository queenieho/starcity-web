(ns starcity.controllers.about
  (:require [starcity.views.about :as view]
            [ring.util.response :as response]
            [starcity.controllers.utils :refer :all]))

;; =============================================================================
;; API
;; =============================================================================

(defn show-about
  [req]
  (ok (view/about req)))
