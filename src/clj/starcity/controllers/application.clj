(ns starcity.controllers.application
  (:require [starcity.views.application :as view]
            [starcity.models.application :as application]
            [starcity.controllers.utils :refer :all]))

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
    (ok (if locked
          (view/locked (:completed params))
          (view/application sections)))))
