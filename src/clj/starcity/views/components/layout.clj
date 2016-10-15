(ns starcity.views.components.layout
  (:require [hiccup.def :refer [defelem]]))

(defelem box
  "A 'box' with rounded corners, a slight shadow for depth, and a border."
  [& content]
  [:div.box content])

(defelem section
  [& content]
  [:section.section content])

(defelem container
  [& content]
  [:div.container content])

(defelem columns
  [& content]
  [:div.columns content])

(defelem column
  [& content]
  [:div.column content])
