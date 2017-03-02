(ns starcity.views.admin
  (:require [starcity.views.components.loading :as l]
            [starcity.views.page :as p]))

(def admin
  (p/app
   "admin"
   (p/title "Admin")
   (p/css "antd.css")
   [:div#app l/hero-section]))
