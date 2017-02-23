(ns admin.properties.views.common)

(defn- metric [heading content]
  [:div.level-item.has-text-centered
   [:p.heading heading]
   [:p.subtitle content]])
