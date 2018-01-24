(ns starcity.controllers.faq
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]))

(html/defsnippet faq "templates/faq.html" [:main] [])


(defn show
  "Show the FAQ page."
  [req]
  (common/render-ok
   (common/page req {:css-bundles ["public.css"]
                     :js-bundles  ["main.js"]
                     :main        (faq)})))
