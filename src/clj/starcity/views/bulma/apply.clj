(ns starcity.views.bulma.apply
  (:require [starcity.views.page :as p]))

(def loading
  [:section.hero.is-fullheight
   [:div.hero-body
    [:div.container.has-text-centered
     [:h1.is-3.subtitle "Loading..."]
     [:div.sk-double-bounce
      [:div.sk-child.sk-double-bounce1]
      [:div.sk-child.sk-double-bounce2]]]]])

(def apply
  (p/cljs-page "apply"
               (p/title "Apply")
               (p/navbar)
               (p/cljs "apply" loading)
               p/footer))
