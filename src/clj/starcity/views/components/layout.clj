(ns starcity.views.components.layout
  (:require [hiccup.def :refer [defelem]]))

(defelem box
  "A 'box' with rounded corners, a slight shadow for depth, and a border."
  [& content]
  [:div.box content])
