(ns starcity.controllers.privacy
  (:require [starcity.views.legal.privacy :as view]
            [ring.util.response :as response]
            [starcity.controllers.utils :refer :all]))

;; =============================================================================
;; API
;; =============================================================================

(defn show-privacy
  [req]
  (ok (view/privacy)))
