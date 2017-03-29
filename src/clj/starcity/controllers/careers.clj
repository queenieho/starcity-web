(ns starcity.controllers.careers
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]
            [starcity.views.base :as base]))

(html/defsnippet careers "templates/careers.html" [:main] [])

(defn show
  "Show the Careers page."
  [req]
  (common/render-ok (base/public-base req :main (careers))))
