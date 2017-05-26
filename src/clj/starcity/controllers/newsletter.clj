(ns starcity.controllers.newsletter
  (:require [datomic.api :as d]
            [facade.core :as facade]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :as response]
            [starcity.controllers.common :as common]
            [starcity.datomic :refer [conn]]
            [starcity.models.cmd :as cmd]))

(html/defsnippet newsletter "templates/subscribed-newsletter.html" [:main] [])

(defn show
  [req]
  (common/render-ok
   (facade/public req
                  :css-bundles ["public.css"]
                  :js-bundles ["main.js"]
                  :main (newsletter))))

(defn subscribe!
  "Subscribe the specified email address (params) to our newsletter."
  [req]
  (when-let [email (get-in req [:params :email])]
    (d/transact-async conn [(cmd/subscribe-to-newsletter email)]))
  (response/redirect "/newsletter"))
