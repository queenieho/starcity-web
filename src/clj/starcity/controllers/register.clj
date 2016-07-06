(ns starcity.controllers.register
  (:require [starcity.views.register :as view]
            [starcity.controllers.utils :refer :all]
            [starcity.services.mailchimp :as mailchimp]
            [taoensso.timbre :refer [infof]]
            [ring.util.response :as response]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- log-subscriber-request
  [email {:keys [status body]}]
  (infof "MAILCHIMP :: add subscriber :: email - %s :: status - %s :: body - %s"
         email status body))

;; =============================================================================
;; API
;; =============================================================================

(defn register-user!
  [{:keys [params] :as req}]
  (if-let [email (:email params)]
    (do
      (mailchimp/add-interested-subscriber! email (partial log-subscriber-request email))
      (ok (view/registration email)))
    (response/redirect "/")))
