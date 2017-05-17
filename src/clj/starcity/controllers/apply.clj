(ns starcity.controllers.apply
  (:require [net.cgrand.enlive-html :as html]
            [starcity.config.stripe :refer [public-key]]
            [starcity.controllers.common :as common]
            [starcity.models.apply :refer [application-fee]]
            [starcity.views.base :as base]))

(html/defsnippet apply-content "templates/apply.html" [:section] []
  [:section] (html/append (base/loading-fs)))

(apply-content)

(defn show
  "Show the Apply app."
  [req]
  (common/render-ok
   (base/app-base req "apply"
                  :content (apply-content)
                  :navbar (base/app-navbar)
                  :chatlio? true
                  :scripts ["https://checkout.stripe.com/checkout.js"]
                  :json [["stripe" {:amount application-fee
                                    :key    public-key}]]
                  :stylesheets [base/font-awesome])))
