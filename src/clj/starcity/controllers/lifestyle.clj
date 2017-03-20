(ns starcity.controllers.lifestyle
  (:require [selmer.parser :as selmer]
            [starcity.views.common :refer [public-defaults]]))

(defn show
  "Show the lifestyle page."
  [req]
  (selmer/render-file "lifestyle.html" (public-defaults req)))
