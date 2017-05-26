(ns starcity.controllers.communities
  (:require [facade
             [core :as facade]
             [snippets :as snippets]]
            [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]))

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
  (->> (facade/public req
                      :svg (mission-svg)
                      :header (snippets/public-header :communities)
                      :css-bundles ["public.css"]
                      :js-bundles ["main.js"]
                      :main (mission-main)
                      :fonts [mission-fonts])
       (common/render-ok)))

(defn show-soma
  "Show the SoMa community page."
  [req]
  (->> (facade/public req
                      :svg (soma-svg)
                      :header (snippets/public-header :communities)
                      :css-bundles ["public.css"]
                      :js-bundles ["main.js"]
                      :main (soma-main)
                      :fonts [soma-fonts])
       (common/render-ok)))
