(ns starcity.controllers.application
  (:require [starcity.views.application :as view]
            [starcity.models.application :as application]
            [starcity.controllers.utils :refer :all]))

;; =============================================================================
;; Helpers
;; =============================================================================

;; 1. Logistics is always enabled
;; 2. Can view checks iff #{:rental-application/desired-lease :rental-application/desired-availability} are non-nil
;; 3. Can view community fitness iff #{}

;; =============================================================================
;; API
;; =============================================================================

(defn show-application
  "Respond 200 OK with the application page."
  [{:keys [identity] :as req}]
  (let [sections (application/current-steps (:db/id identity))]
    (ok (view/application sections))))

;; TODO:
;; NOTE: Use multimethod!?
;; (def restrictions
;;   )
