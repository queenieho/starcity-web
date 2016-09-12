(ns starcity.controllers.bulma
  (:require [starcity.views.bulma :as view]
            [ring.util.response :as response]
            [starcity.controllers.utils :refer :all]))

(defn show-bulma
  [req]
  (ok (view/bulma req)))
