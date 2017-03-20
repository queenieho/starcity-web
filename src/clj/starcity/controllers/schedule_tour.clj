(ns starcity.controllers.schedule-tour
  (:require [selmer.parser :as selmer]
            [starcity.views.common :refer [public-defaults]]))

(defn show
  "Show the Schedule Tour page."
  [req]
  (selmer/render-file "schedule-tour.html" (public-defaults req)))
