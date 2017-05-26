(ns starcity.controllers.careers
  (:require [facade.core :as facade]
            [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]))

(html/defsnippet careers "templates/careers.html" [:main] [])

(defn show
  "Show the Careers page."
  [req]
  (common/render-ok
   (facade/public req
                  :css-bundles ["public.css"]
                  :js-bundles ["main.js"]
                  :main (careers))))
