(ns starcity.controllers.faq
  (:require [facade.core :as facade]
            [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]))

(html/defsnippet faq "templates/faq.html" [:main] [])

(defn show
  "Show the FAQ page."
  [req]
  (common/render-ok
   (facade/public req
                  :css-bundles ["public.css"]
                  :js-bundles ["main.js"]
                  :main (faq))))
