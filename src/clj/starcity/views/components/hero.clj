(ns starcity.views.components.hero)

(defn hero
  "Construct a hero."
  [attrs? & content]
  (let [attrs (if (map? attrs?) attrs? {})]
    [:section.hero attrs
     (when-not (map? attrs?)
       attrs?)
     content]))

(defn body [& content]
  [:div.hero-body content])

(defn head [& content]
  [:div.hero-head content])
