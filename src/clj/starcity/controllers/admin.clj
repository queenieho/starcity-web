(ns starcity.controllers.admin
  (:require [optimus.link :as link]
            [starcity.controllers.common :as common]
            [starcity.views.base :as base]))

(defn show
  "Show the Admin dashboard app."
  [req]
  (common/render-ok
   (base/app-base req "admin"
                  :stylesheets (link/bundle-paths req ["antd.css"])
                  :fonts [base/lato-fonts])))
