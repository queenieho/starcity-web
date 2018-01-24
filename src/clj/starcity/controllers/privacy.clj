(ns starcity.controllers.privacy
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]))

(html/defsnippet privacy "templates/privacy.html" [:main] [])

(defn show
  "Show the Privacy Policy page."
  [req]
  (common/render-ok
   (common/page req {:css-bundles ["public.css"]
                     :js-bundles  ["main.js"]
                     :main        (privacy)})))
