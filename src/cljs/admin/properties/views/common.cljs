(ns admin.properties.views.common)

(defn- metric [heading content]
  [:div.column.has-text-centered
   [:p.heading heading]
   [:p.subtitle content]])
