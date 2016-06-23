(ns starcity.controllers.availability
  (:require [starcity.views.availability :as view]
            [ring.util.response :as response]
            [starcity.datomic :refer [conn]]
            [starcity.datomic.util :refer [find-all-by]]
            [starcity.controllers.utils :refer :all]
            [datomic.api :as d]))

;; =============================================================================
;; API
;; =============================================================================

(defn show-availability
  [req]
  (let [gilbert (first (find-all-by (d/db conn) :property/name))]
    ;; TODO: Don't HC
    (ok (view/availability 5))))
