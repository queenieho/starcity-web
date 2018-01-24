(ns starcity.controllers.lifestyle
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]))

(html/defsnippet svg "templates/lifestyle/svg.html" [:svg] [])
(html/defsnippet main "templates/lifestyle.html" [:main] [])

(defn show
  "Show the lifestyle page."
  [req]
  (->> (common/page req {:main        (main)
                         :svg         (svg)
                         :header      (common/header :lifestyle)
                         :css-bundles ["public.css"]
                         :js-bundles  ["main.js"]})
       (common/render-ok)))
