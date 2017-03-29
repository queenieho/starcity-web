(ns starcity.controllers.terms
  (:require [net.cgrand.enlive-html :as html]
            [starcity.controllers.common :as common]
            [starcity.views.base :as base]))

(html/defsnippet terms "templates/terms.html" [:main] [])

(defn show
  "Show the Terms of Service page."
  [req]
  (common/render-ok (base/public-base req :main (terms))))
