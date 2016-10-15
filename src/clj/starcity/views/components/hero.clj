(ns starcity.views.components.hero
  "http://bulma.io/documentation/layout/hero/"
  (:require [hiccup.def :refer [defelem]]))

(def ^:private gradient-alpha 0.45)

(defn- gradient-background-image
  [url]
  {:style (format "background-image: linear-gradient(rgba(0, 0, 0, %f), rgba(0, 0, 0, %f)), url('%s');"
                  (* 0.8 gradient-alpha) (* 1.4 gradient-alpha) url)})

(defelem background-image
  [img-url & content]
  [:section.hero.has-background-image
   (gradient-background-image img-url)
   content])

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
