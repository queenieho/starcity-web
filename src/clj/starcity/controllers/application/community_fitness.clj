(ns starcity.controllers.application.community-fitness
  (:require [ring.util.response :as response]
            [starcity.auth :refer [user-passes]]
            [starcity.controllers.utils :refer :all]
            [starcity.models.application :as application]
            [starcity.views.application.community-fitness :as view]
            [starcity.views.error :as error-view]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- can-view-community-fitness?
  [{:keys [identity] :as req}]
  (when-let [application-id (:db/id (application/by-account-id (:db/id identity)))]
    (application/personal-information-complete? application-id)))

;; =============================================================================
;; API
;; =============================================================================

(defn show-community-fitness
  [{:keys [identity] :as req}]
  (let [current-steps (application/current-steps (:db/id identity))]
    (ok (view/community-fitness current-steps))))

(defn save!
  [{:keys [identity params] :as req}]
  )

;; TODO:
(def restrictions
  (let [err "Please complete the <a href='/application/personal'>Personal Information</a> step first."]
    {:handler  {:and [(user-passes can-view-community-fitness?)]}
     :on-error (fn [req _]
                 (-> (error-view/error err)
                     (response/response)
                     (assoc :status 403)))}))
