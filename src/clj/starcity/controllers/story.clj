(ns starcity.controllers.story
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]
            [starcity.views.base :as base]))

(html/defsnippet svg "templates/story/svg.html" [:svg] [])
(html/defsnippet main "templates/story.html" [:main] [])

(defn show
  "Show the story page."
  [req]
  (->> (base/public-base req
                         :main (main)
                         :svg (svg)
                         :header (base/header :story))
       (common/render-ok)))
