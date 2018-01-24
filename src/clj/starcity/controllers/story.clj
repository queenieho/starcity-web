(ns starcity.controllers.story
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]))

(html/defsnippet svg "templates/story/svg.html" [:svg] [])
(html/defsnippet main "templates/story.html" [:main] [])

(defn show
  "Show the story page."
  [req]
  (->> (common/page req {:main        (main)
                         :css-bundles ["public.css"]
                         :js-bundles  ["main.js"]
                         :svg         (svg)
                         :header      (common/header :story)})
       (common/render-ok)))
