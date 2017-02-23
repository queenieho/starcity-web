(ns admin.properties.views
  (:require [admin.components.header :as h]
            [admin.content :refer [app-content]]
            [admin.properties.views.overview :as overview]
            [admin.properties.views.entry :as entry]
            [ant-ui.core :as a]))

(defmethod app-content :properties [_]
  [a/layout
   (h/header [:div])
   [overview/content]])

(defmethod app-content :property [_]
  [a/layout
   (h/header [:div])
   [entry/content]])
