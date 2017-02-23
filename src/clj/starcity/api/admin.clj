(ns starcity.api.admin
  (:require [compojure.core :refer [context defroutes]]
            [starcity.api.admin
             [accounts :as accounts]
             [checks :as checks]
             [income :as income]
             [licenses :as licenses]
             [properties :as properties]]))

(defroutes routes
  (context "/accounts" [] accounts/routes)
  (context "/checks" [] checks/routes)
  (context "/income-file" [] income/routes)
  (context "/licenses" [] licenses/routes)
  (context "/properties" [] properties/routes))
