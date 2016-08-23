(ns starcity.controllers.onboarding
  (:require [starcity.views.onboarding :refer [base]]
            [starcity.controllers.utils :refer [ok]]))

(defn show [req]
  (ok (base req)))
