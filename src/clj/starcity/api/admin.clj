(ns starcity.api.admin
  (:require [compojure.core :refer [context defroutes]]
            [starcity.api.admin
             [accounts :as accounts]
             [applications :as applications]
             [income :as income]]))

(defroutes routes
  (context "/accounts" [] accounts/routes)
  (context "/applications" [] applications/routes)
  (context "/income-file" [] income/routes))
