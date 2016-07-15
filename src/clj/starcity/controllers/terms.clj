(ns starcity.controllers.terms
  (:require [starcity.views.terms :as view]
            [ring.util.response :as response]
            [starcity.controllers.utils :refer :all]))

;; =============================================================================
;; API
;; =============================================================================

(defn show-terms
  [req]
  (ok (view/terms)))
