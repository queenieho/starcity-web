(ns starcity.controllers.terms
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]))

(html/defsnippet terms "templates/terms.html" [:main] [])

(defn show
  "Show the Terms of Service page."
  [req]
  (common/render-ok
   (common/page req {:css-bundles ["public.css"]
                     :js-bundles  ["main.js"]
                     :main        (terms)})))
