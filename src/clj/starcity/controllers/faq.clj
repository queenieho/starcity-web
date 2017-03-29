(ns starcity.controllers.faq
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]
            [starcity.views.base :as base]))

(html/defsnippet faq "templates/faq.html" [:main] [])

(defn show
  "Show the FAQ page."
  [req]
  (common/render-ok (base/public-base req :main (faq))))
