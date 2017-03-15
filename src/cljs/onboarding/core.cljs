(ns onboarding.core
  (:require [ant-ui.core :as a]
            [ant-ui.fx]
            [ant-ui.locales :refer [en-US]]
            [goog.dom :as gdom]
            [onboarding.events]
            [onboarding.routes :as routes]
            [onboarding.subs]
            [onboarding.views :as views]
            [reagent.core :as r]
            [toolbelt.re-frame.fx]
            [re-frame.core :refer [dispatch-sync]]))

(enable-console-print!)

(defn render []
  (r/render
   [a/locale-provider {:locale en-US}
    [views/app]]
   (gdom/getElement "onboarding")))

(defn ^:export run []
  (dispatch-sync [:app/init])
  (routes/hook-browser-navigation!)
  (render))
