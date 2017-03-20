(ns starcity.controllers.communities
  (:require [selmer.parser :as selmer]
            [starcity.views.common :refer [public-defaults]]))

(defn show-mission
  "Show the Mission community page."
  [req]
  (selmer/render-file "mission.html" (public-defaults req)))

(defn show-soma
  "Show the SoMa community page."
  [req]
  (selmer/render-file "soma.html" (public-defaults req)))
