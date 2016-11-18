(ns starcity.views.dashboard
  (:require [starcity.views.page :as p]
            [starcity.views.components.loading :as l]))

(def mars
  (p/cljs-page
   "mars"
   (p/title "Members")
   [:div#app l/hero-section]))
