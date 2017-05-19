(ns starcity.controllers.dashboard
  (:require [facade.core :as facade]
            [starcity.controllers.common :as common]))

(defn show
  "Show the member dashboard (MARS)."
  [req]
  (common/render-ok
   (facade/app req "mars"
               :fonts ["https://fonts.googleapis.com/css?family=Josefin+Sans|Work+Sans:400,600"]
               :title "Starcity Members"
               :css-bundles ["antd.css" "styles.css"])))
