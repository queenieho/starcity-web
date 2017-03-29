(ns starcity.controllers.lifestyle
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]
            [starcity.views.base :as base]))

(html/defsnippet svg "templates/lifestyle/svg.html" [:svg] [])
(html/defsnippet main "templates/lifestyle.html" [:main] [])

(defn show
  "Show the lifestyle page."
  [req]
  (->> (base/public-base req
                         :main (main)
                         :svg (svg)
                         :header (base/header :lifestyle))
       (common/render-ok)))
