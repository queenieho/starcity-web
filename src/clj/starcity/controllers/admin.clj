(ns starcity.controllers.admin
  (:require [starcity.views.admin :as view]
            [starcity.controllers.utils :refer [ok]]))

(defn show [req]
  (ok view/admin))
