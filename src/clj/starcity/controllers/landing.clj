(ns starcity.controllers.landing
  (:require [ring.util.response :as response]
            [starcity.controllers.utils :refer :all]
            [starcity.services.mailchimp :as mailchimp]
            [starcity.views.landing :as view]
            [taoensso.timbre :as timbre]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- log-subscriber-request
  [email {:keys [status body]}]
  (timbre/info :mailchimp/new-subscriber {:email email :status status}))

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
