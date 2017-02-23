(ns admin.components.content
  (:require [ant-ui.core :as a]))

(defn content [& children]
  [a/layout-content {:class "admin-content"}
   (map-indexed
    #(with-meta %2 {:key %1})
    children)])
