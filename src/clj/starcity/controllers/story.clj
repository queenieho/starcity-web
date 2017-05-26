(ns starcity.controllers.story
  (:require [facade
             [core :as facade]
             [snippets :as snippets]]
            [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]))

(html/defsnippet svg "templates/story/svg.html" [:svg] [])
(html/defsnippet main "templates/story.html" [:main] [])

(defn show
  "Show the story page."
  [req]
  (->> (facade/public req
                      :main (main)
                      :css-bundles ["public.css"]
                      :js-bundles ["main.js"]
                      :svg (svg)
                      :header (snippets/public-header :story))
       (common/render-ok)))
