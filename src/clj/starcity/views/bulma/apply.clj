(ns starcity.views.bulma.apply
  (:require [starcity.views.page :as p]
            [starcity.views.components.loading :as l]))



(def apply
  (p/cljs-page "apply"
               (p/title "Apply")
               (p/navbar)
               [:section#apply.section l/hero-section]
               p/footer))
