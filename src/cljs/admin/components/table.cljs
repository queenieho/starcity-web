(ns admin.components.table
  (:require [clojure.string :as string]))

(defn column
  [key & {:keys [render title]}]
  (let [render (if render
                 (fn [a b]
                   (render (js->clj a :keywordize-keys true)
                           (js->clj b :keywordize-keys true)))
                 identity)]
    {:title     (or title (string/capitalize key))
     :key       key
     :dataIndex key
     :render    render}))
