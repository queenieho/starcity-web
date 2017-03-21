(ns starcity.controllers.dashboard
  (:require [optimus.link :as link]
            [selmer.parser :as selmer]
            [starcity.controllers.common :as common]
            [starcity.views.common :refer [app-defaults]]))

(defn show
  "Show the member dashboard (MARS)."
  [req]
  (->> (app-defaults req "mars" :stylesheets (link/bundle-paths req ["antd.css"]))
       (selmer/render-file "mars.html")
       (common/ok)))
