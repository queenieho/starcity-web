(ns starcity.controllers.onboarding
  (:require [net.cgrand.enlive-html :as html]
            [optimus.link :as link]
            [starcity.controllers.common :as common]
            [starcity.config.stripe :refer [public-key]]
            [starcity.views.base :as base]))

(html/defsnippet content "templates/onboarding.html" [:section] []
  [:section] (html/append (base/loading-fs)))

(defn show
  "Show the Onboarding app."
  [req]
  (common/render-ok
   (base/app-base req "onboarding"
                  :content (content)
                  :navbar (base/app-navbar)
                  :json [["stripe" {:key public-key}]]
                  :stylesheets (link/bundle-paths req ["antd.css"]))))
