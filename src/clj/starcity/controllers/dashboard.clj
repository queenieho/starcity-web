(ns starcity.controllers.dashboard
  (:require [starcity.views.dashboard :refer [base]]
            [starcity.controllers.utils :refer [ok]]))

(defn show [req]
  (ok (base req)))
