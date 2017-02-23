(ns admin.components.level)

(defn- render-overview-item
  [{:keys [title content format] :or {format identity}}]
  [:div.level-item.has-text-centered
   [:p.heading title]
   [:p.subtitle.is-6 (format content)]])

(defn overview
  [& items]
  [:div.level
   (->> items
        (remove nil?)
        (map-indexed #(with-meta (render-overview-item %2) {:key %1})))])

(defn overview-item
  "Construct an overview item."
  ([title content]
   (overview-item title content identity))
  ([title content format]
   {:title   title
    :content content
    :format  format}))
