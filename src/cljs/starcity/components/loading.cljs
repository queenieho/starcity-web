(ns starcity.components.loading)

(defn container []
  [:div.container.has-text-centered
   [:h1.is-3.subtitle "Loading..."]
   [:div.sk-double-bounce
    [:div.sk-child.sk-double-bounce1]
    [:div.sk-child.sk-double-bounce2]]])

(defn fill-container []
  [:section.hero.is-fullheight
  [:div.hero-body (container)]])
