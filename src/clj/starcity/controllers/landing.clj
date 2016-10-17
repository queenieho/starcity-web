(ns starcity.controllers.landing
  (:require [starcity.views.bulma.landing :as view]
            [starcity.services.mailchimp :as mailchimp]
            [starcity.controllers.utils :refer :all]
            [ring.util.response :as response]
            [taoensso.timbre :refer [infof]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- log-subscriber-request
  [email {:keys [status body]}]
  (infof "MAILCHIMP :: add subscriber :: email - %s :: status - %s :: body - %s"
         email status body))

(def ^:private url-after-newsletter-signup
  "/?newsletter=subscribed#newsletter")

;; =============================================================================
;; API
;; =============================================================================

(defn newsletter-signup [{:keys [params] :as req}]
  (if-let [email (:email params)]
    (do
      (mailchimp/add-interested-subscriber! email (partial log-subscriber-request email))
      (response/redirect url-after-newsletter-signup))
    (response/redirect "/")))

(def show-landing
  (comp ok view/landing))
