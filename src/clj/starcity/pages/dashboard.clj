(ns starcity.pages.dashboard
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [ok]]
            [ring.util.response :as response]))


;; =============================================================================
;; Components

;; =============================================================================
;; API

(defn render [req]
  (ok (base
       [:div.container
        [:div.page-header
         [:h1 "This is the dashboard"]]])))
