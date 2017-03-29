(ns starcity.controllers.newsletter
  (:require [datomic.api :as d]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :as response]
            [starcity.controllers.common :as common]
            [starcity.datomic :refer [conn]]
            [starcity.models.cmd :as cmd]
            [starcity.views.base :as base]))

(html/defsnippet newsletter "templates/subscribed-newsletter.html" [:main] [])

(defn show
  [req]
  (common/render-ok
   (base/public-base req :main (newsletter))))

(defn subscribe!
  "Subscribe the specified email address (params) to our newsletter."
  [req]
  (when-let [email (get-in req [:params :email])]
    (d/transact-async conn [(cmd/subscribe-to-newsletter email)]))
  (response/redirect "/newsletter"))
