(ns starcity.pages.dashboard
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]
            [starcity.router :refer [route]]
            [starcity.environment :refer [environment]]
            [ring.util.response :as response]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Components

(defn- render-dashboard [req]
  (let [username (get-in req [:session :identity :account/email])]
    (base
     [:div#app]
     :js ["app/main.js"]
     :css ["forms.css"]
     :cljs-devtools? (= :development environment))))

;; =============================================================================
;; API

(defmethod route [:dashboard :get] [_ req]
  (if-not (authenticated? req)
    (throw-unauthorized)
    (ok (render-dashboard req))))
