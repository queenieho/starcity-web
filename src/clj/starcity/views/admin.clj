(ns starcity.views.admin
  (:require [starcity.views.page :as p]))

(def admin (p/cljs-page "admin"
                        (p/title "Admin")
                        (p/cljs "app")))
