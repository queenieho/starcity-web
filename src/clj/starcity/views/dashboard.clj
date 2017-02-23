(ns starcity.views.dashboard
  (:require [starcity.views.components.loading :as l]
            [starcity.views.page :as p]))

(def mars
  (p/cljs-page
   "mars"
   (p/title "Members")
   (p/css "/assets/css/antd.css")
   [:div#app l/hero-section]))
