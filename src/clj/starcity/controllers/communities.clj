(ns starcity.controllers.communities
  (:require [starcity.views.communities :as view]
            [ring.util.response :as response]
            [starcity.controllers.utils :refer :all]
            [datomic.api :as d]))

;; =============================================================================
;; API
;; =============================================================================

(def show-communities
  (comp ok view/communities))

(def show-mission
  (comp ok view/mission))

(def show-soma
  (comp ok view/soma))
