(ns starcity.views.dashboard
  (:require [starcity.views.page :as p]
            [starcity.views.components.loading :as l]
            [starcity.config.plaid :as config]
            [starcity.config.stripe :as stripe]
            [starcity.countries :refer [countries]]))

(def mars
  (p/cljs-page
   "mars"
   (p/title "Members")
   (p/css "/assets/css/antd.css")
   [:div#app l/hero-section]))
