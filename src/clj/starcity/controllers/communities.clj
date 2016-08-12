(ns starcity.controllers.communities
  (:require [starcity.views.communities :as view]
            [ring.util.response :as response]
            [starcity.models.property :as property]
            [starcity.controllers.utils :refer :all]
            [datomic.api :as d]))

;; =============================================================================
;; API
;; =============================================================================

(defn show-communities
  [req]
  (let [properties (property/many [:db/id
                                   :property/name
                                   :property/available-on
                                   :property/cover-image-url
                                   :property/description
                                   :property/internal-name
                                   :property/upcoming
                                   {:property/licenses [:property-license/base-price
                                                        {:property-license/license [:license/term]}]}])]
    (ok (view/communities req properties))))
