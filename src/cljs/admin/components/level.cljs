(ns admin.components.level
  (:require [reagent.core :as r]
            [clojure.string :as string]))

(defn- render-overview-item
  [{:keys [title content format] :or {format identity}}]
  [:div.column.has-text-centered
   (when-not (string/blank? title)
     [:p.heading title])
   (if (vector? content)
     (r/as-element content)
     [:p.subtitle.is-6 (format content)])])

(defn overview
  [& items]
  [:div.columns
   (->> items
        (remove nil?)
        (map-indexed #(with-meta (render-overview-item %2) {:key %1})))])

(defn overview-item
  "Construct an overview item."
  ([content]
   (overview-item "" content identity))
  ([title content]
   (overview-item title content identity))
  ([title content format]
   {:title   title
    :content content
    :format  format}))
