(ns starcity.controllers.admin
  (:require [facade.core :as facade]
            [starcity.controllers.common :as common]))

(defn show
  "Show the Admin dashboard app."
  [req]
  (common/render-ok
   (facade/app req "admin"
               :css-bundles ["styles.css" "antd.css"]
               :fonts [facade/lato-fonts])))
