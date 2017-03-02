(ns starcity.controllers.apply
  (:require [starcity.views.apply :as view]
            [starcity.controllers.utils :refer [ok]]))

(def show-apply
  (comp ok view/apply))
