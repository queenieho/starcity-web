(ns starcity.controllers.elm
  (:require [starcity.views.elm :refer [base]]
            [starcity.controllers.utils :refer [ok]]))

(defn show [req]
  (ok (base)))
