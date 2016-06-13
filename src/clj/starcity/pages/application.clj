(ns starcity.pages.application
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]
            [starcity.environment :refer [environment]]
            [ring.util.response :as response]))


;; =============================================================================
;; Components

;; =============================================================================
;; API

(defn render [req]
  (let [username (get-in req [:session :identity :account/email])]
    (base
     [:div#app]
     :js ["app/main.js"]
     :css ["forms.css"]
     :cljs-devtools? (= :development environment))))
