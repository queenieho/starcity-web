(ns starcity.controllers.dashboard
  (:require [starcity.views.dashboard :as view]
            [starcity.controllers.utils :refer [ok]]))

(defn show [req]
  (ok view/mars))
