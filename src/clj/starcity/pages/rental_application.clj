(ns starcity.pages.rental-application
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]
            [ring.util.response :as response]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Components

(defn- render-application [req]
  (let [username (get-in req [:session :identity :account/email])]
    (base
     [:div#app]
     ;; TODO: Better workflow regarding minification...optimus
     :js ["app/main.js"])))

;; =============================================================================
;; API

(defn handle [req]
  (if-not (authenticated? req)
    (throw-unauthorized)
    (ok (render-application req))))
