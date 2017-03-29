(ns starcity.controllers.privacy
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]
            [starcity.views.base :as base]))

(html/defsnippet privacy "templates/privacy.html" [:main] [])

(defn show
  "Show the Privacy Policy page."
  [req]
  (common/render-ok (base/public-base req :main (privacy))))
