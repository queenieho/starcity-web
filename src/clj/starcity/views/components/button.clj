(ns starcity.views.components.button
  (:require [hiccup.def :refer [defelem]]))

(defelem button
  ([content]
   (button :button content))
  ([type content]
   (if (= :link type)
     [:a.button content]
     [:button.button {:type (name type)} content])))

(defelem primary
  ([content]
   (primary :button content))
  ([type content]
   [:button.button.is-primary {:type (name type)} content]))

(defelem link
  [content]
  (button {:class "is-link"} :link content))
