(ns starcity.views.dashboard
  (:require [starcity.views.components.loading :as l]
            [starcity.views.page :as p]))

(def mars
  (p/app
   "mars"
   (p/title "Members")
   (p/css "antd.css")
   [:div#app l/hero-section]))
