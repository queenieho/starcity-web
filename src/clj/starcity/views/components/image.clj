(ns starcity.views.components.image
  (:require [hiccup.def :refer [defelem]]))

(defelem image
  ([src]
   (image src false))
  ([src circular]
   [:figure.image [:img {:src src :class (when circular "is-circular")}]]))
