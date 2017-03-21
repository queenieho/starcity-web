(ns admin.core
  (:require [goog.dom :as gdom]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch-sync]]
            [admin.db]
            [admin.events]
            [admin.subs]
            [admin.routes :as routes]
            [admin.views :as views]
            [ant-ui.core :as a]
            [ant-ui.fx]
            [ant-ui.locales :refer [en-US]]
            [reagent.core :as r]
            [toolbelt.re-frame.fx]))

(enable-console-print!)

(defn render []
  (r/render
   [a/locale-provider {:locale en-US}
    [views/app]]
   (gdom/getElement "admin")))

(defn ^:export run []
  (routes/hook-browser-navigation!)
  (dispatch-sync [:app/initialize])
  (render))
