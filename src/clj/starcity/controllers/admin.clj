(ns starcity.controllers.admin
  (:require [starcity.views.admin :refer [base]]
            [starcity.controllers.utils :refer [ok]]))

(defn show [req]
  (ok (base)))
