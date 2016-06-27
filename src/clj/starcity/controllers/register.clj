(ns starcity.controllers.register
  (:require [starcity.views.register :as view]
            [starcity.controllers.utils :refer :all]
            [starcity.services.mailchimp :as mailchimp]
            [taoensso.timbre :refer [infof]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- log-subscriber-request
  [email {:keys [status body]}]
  (infof "MAILCHIMP :: add subscriber :: email - %s :: status - %s :: body - %s"
         email status body))

(def ^:private navbar
  [:nav.navbar
   [:div.container
    [:div.navbar-header
     [:a.navbar-brand {:href "#"}
      [:img {:alt "Starcity" :src "/assets/img/starcity-brand-icon-white.png"}]]]]])

;; =============================================================================
;; API
;; =============================================================================

(defn register-user!
  [{:keys [params] :as req}]
  (let [email (:email params)]
    (mailchimp/add-interested-subscriber! email (partial log-subscriber-request email))
    (ok (view/post-registration email))))
