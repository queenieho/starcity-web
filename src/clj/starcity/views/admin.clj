(ns starcity.views.admin
  (:require [starcity.views.page :as p]
            [starcity.views.components.loading :as l]))

(def admin
  (p/cljs-page
   "admin"
   (p/title "Admin")
   [:div#app l/hero-section]))
