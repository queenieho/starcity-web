(ns starcity.controllers.newsletter
  (:require [datomic.api :as d]
            [selmer.parser :as selmer]
            [starcity.controllers.common :as common]
            [starcity.datomic :refer [conn]]
            [starcity.models.cmd :as cmd]
            [starcity.views.common :refer [public-defaults]]
            [ring.util.response :as response]))

(defn show
  [req]
  (common/ok
   (selmer/render-file "subscribed-newsletter.html" (public-defaults req))))

(defn subscribe!
  "Subscribe the specified email address (params) to our newsletter."
  [req]
  (when-let [email (get-in req [:params :email])]
    (d/transact conn [(cmd/subscribe-to-newsletter email)]))
  (response/redirect "/newsletter"))
