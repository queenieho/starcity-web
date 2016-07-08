(ns starcity.controllers.application.submit
  (:require [starcity.models.application :as application]
            [starcity.controllers.application.common :as common]
            [starcity.views.application.submit :as view]
            [starcity.controllers.utils :refer :all]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- can-view-submit?
  [{:keys [identity] :as req}]
  (when-let [application-id (:db/id (application/by-account-id (:db/id identity)))]
    (application/community-fitness-complete? application-id)))

;; =============================================================================
;; API
;; =============================================================================

(defn show-submit
  [{:keys [identity] :as req}]
  (let [current-steps (application/current-steps (:db/id identity))
        ;; data          (pull-data (:db/id identity))
        ]
    (ok (view/submit current-steps))))

(defn submit!
  [{:keys [identity params] :as req}])

(def restrictions
  (common/restrictions "Community Fitness" "/application/community" can-view-submit?))
