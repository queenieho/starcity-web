(ns starcity.controllers.admin
  (:require [optimus.link :as link]
            [selmer.parser :as selmer]
            [starcity.controllers.common :as common]
            [starcity.views.common :refer [app-defaults]]))

(defn show
  "Show the Admin dashboard app."
  [req]
  (->> (app-defaults req "admin" :stylesheets (link/bundle-paths req ["antd.css"]))
       (selmer/render-file "admin.html")
       (common/ok)))
