(ns starcity.controllers.dashboard
  (:require [optimus.link :as link]
            [starcity.controllers.common :as common]
            [starcity.views.base :as base]))

(defn show
  "Show the member dashboard (MARS)."
  [req]
  (common/render-ok
   (base/app-base req "mars"
                  :fonts ["https://fonts.googleapis.com/css?family=Josefin+Sans|Work+Sans:400,600"]
                  :stylesheets (link/bundle-paths req ["antd.css"]))))
