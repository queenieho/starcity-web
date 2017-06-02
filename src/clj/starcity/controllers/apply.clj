(ns starcity.controllers.apply
  (:require [facade
             [core :as facade]
             [snippets :as snippets]]
            [net.cgrand.enlive-html :as html]
            [starcity.config.stripe :refer [public-key]]
            [starcity.controllers.common :as common]
            [starcity.models.apply :refer [application-fee]]))

(html/defsnippet apply-content "templates/apply.html" [:section] []
  [:section] (html/append (snippets/loading-fullscreen)))

(defn show
  "Show the Apply app."
  [req]
  (common/render-ok
   (facade/app req "apply"
                  :content (apply-content)
                  :navbar (snippets/app-navbar)
                  :chatlio? true
                  :scripts ["https://checkout.stripe.com/checkout.js"]
                  :json [["stripe" {:amount application-fee
                                    :key    public-key}]]
                  :css-bundles ["styles.css"]
                  :stylesheets [facade/font-awesome])))
