(ns starcity.views.components.image
  (:require [hiccup.def :refer [defelem]]))

(defelem image [src]
  [:figure.image [:img {:src src}]])
