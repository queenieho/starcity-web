(ns starcity.pages.availability
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [malformed ok]]
            [starcity.router :refer [route]]
            [ring.util.response :as response]))

;; =============================================================================
;; API
;; =============================================================================

(defn render [req]
  (ok (base
       [:div.container
        [:div.page-header
         [:h1 "Here's what is available:"]]
        [:a {:href "/application"} "Apply Now"]])))
