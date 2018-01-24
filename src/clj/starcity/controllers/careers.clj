(ns starcity.controllers.careers
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]))

(html/defsnippet careers "templates/careers.html" [:main] [])


(defn show
  "Show the Careers page."
  [req]
  (common/render-ok
   (common/page req {:css-bundles ["public.css"]
                     :js-bundles  ["main.js"]
                     :main        (careers)})))
