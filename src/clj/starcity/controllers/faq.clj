(ns starcity.controllers.faq
  (:require [starcity.views.faq :as view]
            [ring.util.response :as response]
            [starcity.controllers.utils :refer :all]))

;; =============================================================================
;; API
;; =============================================================================

(defn show-faq
  [req]
  (ok (view/faq req)))
