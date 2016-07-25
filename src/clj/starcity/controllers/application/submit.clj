(ns starcity.controllers.application.submit
  (:require [ring.util.response :as response]
            [starcity.controllers.application.common :as common]
            [starcity.controllers.utils :refer :all]
            [starcity.models
             [account :as account]
             [application :as application]]
            [starcity.services.stripe :as stripe]
            [starcity.views.application.submit :as view]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- can-view-submit?
  [{:keys [identity] :as req}]
  (when-let [application-id (:db/id (application/by-account-id (:db/id identity)))]
    (application/community-fitness-complete? application-id)))

(defn show-submit*
  [{:keys [identity] :as req} & {:keys [errors] :or []}]
  (let [current-steps (application/current-steps (:db/id identity))]
    (view/submit current-steps (:account/email identity) errors)))

(defn- payment-error [req]
  (malformed (show-submit* req :errors ["Something went wrong while processing your payment. Please try again."])))

(defn- charge-application-fee [token email]
  (stripe/charge 2500 token email))

;; =============================================================================
;; API
;; =============================================================================

(defn show-submit
  [req]
  (ok (show-submit* req)))

(defn submit!
  [{:keys [identity params] :as req}]
  (let [{:keys [db/id account/email]} identity]
    (if-let [token (:stripe-token params)]
      (let [{:keys [status body] :as res} (charge-application-fee token email)]
        (if (= status 200)
          (do
            (application/complete! id (:id body))
            (response/redirect "/application?completed=true"))
          (payment-error req)))
      (payment-error req))))

(def restrictions
  (common/restrictions can-view-submit?))
