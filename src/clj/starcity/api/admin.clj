(ns starcity.api.admin
  (:require [compojure.core :refer [context defroutes GET]]
            [starcity.api.admin
             [accounts :as accounts]
             [checks :as checks]
             [income :as income]
             [licenses :as licenses]
             [metrics :as metrics]
             [notes :as notes]
             [properties :as properties]]
            [starcity.auth :as auth]
            [starcity.util.response :as response]
            [starcity.models.account :as account]))

(defroutes routes
  (GET "/" []
       (fn [req]
         (let [requester (auth/requester req)]
           (response/transit-ok {:result {:auth (account/clientize requester)}}))))

  (context "/accounts" [] accounts/routes)
  (context "/checks" [] checks/routes)
  (context "/income-file" [] income/routes)
  (context "/licenses" [] licenses/routes)
  (context "/metrics" [] metrics/routes)
  (context "/notes" [] notes/routes)
  (context "/properties" [] properties/routes))
