(ns starcity.views.communities.common
  (:require [hiccup.def :refer [defelem]]))

(defelem subtitle [& content]
  [:p.subtitle.is-4 content])

(defelem title [& content]
  [:h3.title.is-2
   {:style "margin-bottom: 30px;"}
   content])
