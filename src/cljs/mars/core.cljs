(ns mars.core
  (:require [mars.routes :as routes]
            [mars.events]
            [mars.subs]
            [mars.views :refer [app]]
            [starcity.fx]
            [mars.fx]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch-sync
                                   clear-subscription-cache!]]))

(enable-console-print!)

(defn ^:export run []
  (routes/app-routes)
  ;; (clear-subscription-cache!)
  (dispatch-sync [:app/initialize])
  (r/render [app] (.getElementById js/document "app")))
