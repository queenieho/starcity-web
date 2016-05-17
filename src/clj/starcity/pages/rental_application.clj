(ns starcity.pages.rental-application
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]
            [starcity.middleware :refer [get-environment]]
            [ring.util.response :as response]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Components

(defn- render-application [req]
  (let [environment (get-environment req)
        username    (get-in req [:session :identity :account/email])]
    (base
     [:div#app]
     :js ["app/main.js"]
     :css ["forms.css"]
     :cljs-devtools? (= :development environment))))

;; =============================================================================
;; API

(defn handle [req]
  (if-not (authenticated? req)
    (throw-unauthorized)
    (ok (render-application req))))
