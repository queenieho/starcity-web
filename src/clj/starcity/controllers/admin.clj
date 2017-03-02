(ns starcity.controllers.admin
  (:require [starcity.views.admin :as view]
            [starcity.controllers.utils :refer [ok]]))

(def show
  (comp ok view/admin))
