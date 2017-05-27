(ns starcity.controllers.privacy
  (:require [facade.core :as facade]
            [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]))

(html/defsnippet privacy "templates/privacy.html" [:main] [])

(defn show
  "Show the Privacy Policy page."
  [req]
  (common/render-ok
   (facade/public req
                  :css-bundles ["public.css"]
                  :js-bundles ["main.js"]
                  :main (privacy))))
