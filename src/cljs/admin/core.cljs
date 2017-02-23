(ns admin.core
  (:require [goog.dom :as gdom]
            [reagent.core :as r]
            [re-frame.core :refer [dispatch-sync]]
            [admin.db]
            [admin.events]
            [admin.subs]
            [admin.routes :as routes]
            [admin.views :as views]
            [ant-ui.fx]
            [cljsjs.antd]
            [reagent.core :as r]
            [toolbelt.re-frame.fx]))

(enable-console-print!)

(defn render []
  (r/render [views/app] (gdom/getElement "app")))

(defn ^:export run []
  (routes/hook-browser-navigation!)
  (dispatch-sync [:app/initialize])
  (render))
