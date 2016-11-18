(ns mars.core
  (:require [mars.routes :as routes]
            [mars.views :refer [app]]
            [mars.events]
            [mars.subs]
            [reagent.core :as reagent]
            [re-frame.core :refer [dispatch-sync]]))

(enable-console-print!)

(defn ^:export run []
  (routes/app-routes)
  (dispatch-sync [:app/initialize])
  (reagent/render [app] (.getElementById js/document "app")))
