(ns starcity.controllers.dashboard
  (:require [starcity.views.dashboard :as view]
            [starcity.controllers.utils :refer [ok]]))

(def show
  (comp ok view/mars))
