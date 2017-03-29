(ns starcity.controllers.communities
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]
            [starcity.views.base :as base]))

;; =============================================================================
;; Views
;; =============================================================================

(def ^:private mission-fonts
  "https://fonts.googleapis.com/css?family=Caveat|Eczar:700|Work+Sans:400,600|Rock+Salt")

(html/defsnippet mission-svg "templates/mission/svg.html" [:svg] [])
(html/defsnippet mission-main "templates/mission.html" [:main] [])

(def ^:private soma-fonts
  "https://fonts.googleapis.com/css?family=Caveat|Eczar:700|Work+Sans:400,600|Vast+Shadow")

(html/defsnippet soma-svg "templates/soma/svg.html" [:svg] [])
(html/defsnippet soma-main "templates/soma.html" [:main] [])

;; =============================================================================
;; Handlers
;; =============================================================================

(defn show-mission
  "Show the Mission community page."
  [req]
  (->> (base/public-base req
                         :svg (mission-svg)
                         :header (base/header :communities)
                         :main (mission-main)
                         :fonts [mission-fonts])
       (common/render-ok)))

(defn show-soma
  "Show the SoMa community page."
  [req]
  (->> (base/public-base req
                         :svg (soma-svg)
                         :header (base/header :communities)
                         :main (soma-main)
                         :fonts [soma-fonts])
       (common/render-ok)))
