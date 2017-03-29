(ns starcity.controllers.landing
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]
            [starcity.views.base :as base]))

;; See https://github.com/cgrand/enlive/issues/110
(html/set-ns-parser! base/hickory-parser)

(html/defsnippet svg "templates/landing/svg.html" [:svg] [])
(html/defsnippet header "templates/landing/header.html" [:header] [])
(html/defsnippet main "templates/landing.html" [:main] [])

(defn show
  "Show the landing page."
  [req]
  (->> (base/public-base req :svg (svg) :header (header) :main (main))
       (common/render-ok)))
