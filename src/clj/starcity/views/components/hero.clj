(ns starcity.views.components.hero
  "http://bulma.io/documentation/layout/hero/"
  (:require [hiccup.def :refer [defelem]]))

;; TODO: Come up with a more expressive API
(defelem hero
  "Construct a hero element."
  [& content]
  [:section.hero content])

(defelem body [& content]
  [:div.hero-body content])

(defelem head [& content]
  [:div.hero-head content])

(defelem foot [& content]
  [:div.hero-foot content])
