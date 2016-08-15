(ns starcity.controllers.application
  (:require [starcity.views.application :as view]
            [starcity.models.application :as application]
            [starcity.controllers.utils :refer :all]
            [ring.util.response :as response]))

;; =============================================================================
;; Helpers
;; =============================================================================

;; =============================================================================
;; API
;; =============================================================================

(defn show-application
  "Respond 200 OK with the application page."
  [{:keys [identity params] :as req}]
  (let [sections (application/current-steps (:db/id identity))
        locked   (application/locked? (:db/id identity))]
    (if locked
      (ok (view/locked req (:completed params)))
      (response/redirect "/application/logistics"))))
