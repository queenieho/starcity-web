(ns starcity.pages.availability
  (:require [starcity.pages.base :refer [base]]
            [starcity.pages.util :refer [malformed ok]]
            [starcity.router :refer [route]]
            [ring.util.response :as response]
            [starcity.datomic :refer [conn]]
            [starcity.datomic.util :refer [find-all-by]]
            [datomic.api :as d]))

;; =============================================================================
;; API
;; =============================================================================

(defn render [req]
  ;; NOTE: HC Gilbert -- will need something better when we have additional
  ;; properties!
  (let [gilbert (first (find-all-by (d/db conn) :property/name))]
    (ok (base
        [:div.container
         [:div.page-header
          [:h1 "52 Gilbert Street"]]
         [:p.lead (format "Units available: %s" (:property/units-available gilbert))]
         [:a {:href "/application"} "Apply Now"]]))))
