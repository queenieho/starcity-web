(ns starcity.controllers.lifestyle
  (:require [facade
             [core :as facade]
             [snippets :as snippets]]
            [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]))

(html/defsnippet svg "templates/lifestyle/svg.html" [:svg] [])
(html/defsnippet main "templates/lifestyle.html" [:main] [])

(defn show
  "Show the lifestyle page."
  [req]
  (->> (facade/public req
                      :main (main)
                      :svg (svg)
                      :header (snippets/public-header :lifestyle)
                      :css-bundles ["public.css"]
                      :js-bundles ["main.js"])
       (common/render-ok)))
