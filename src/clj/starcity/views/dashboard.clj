(ns starcity.views.dashboard
  (:require [starcity.views.page :as p]
            [starcity.views.components.layout :as l]))

(def base
  (p/page
   (p/title "Dashboard")
   (l/section
    {:class "is-fullscreen"}
    (l/container
     [:h1.title.is-1 "Work-in-progress. Sorry!"]))))
