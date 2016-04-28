(ns starcity.pages.dashboard
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]
            [ring.util.response :as response]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Components

(defn- dashboard-view [req]
  (let [username (get-in req [:session :identity])]
    (base
     [:div.container
      [:h2 (str "Welcome " username "!")]
      [:a {:href "/logout"} "Log Out"]])))

;; =============================================================================
;; API

(defn handle [req]
  (if-not (authenticated? req)
    (throw-unauthorized)
    (ok (dashboard-view req))))
