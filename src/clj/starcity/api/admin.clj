(ns starcity.api.admin
  (:require [blueprints.models.account :as account]
            [compojure.core :refer [context defroutes GET]]
            [datomic.api :as d]
            [starcity.api.admin.accounts :as accounts]
            [starcity.api.admin.checks :as checks]
            [starcity.api.admin.deposits :as deposits]
            [starcity.api.admin.income :as income]
            [starcity.api.admin.licenses :as licenses]
            [starcity.api.admin.metrics :as metrics]
            [starcity.api.admin.notes :as notes]
            [starcity.api.admin.properties :as properties]
            [starcity.api.admin.referrals :as referrals]
            [starcity.datomic :refer [conn]]
            [starcity.util.request :as req]
            [starcity.util.response :as res]))

(defroutes routes
  (GET "/" []
       (fn [req]
         (let [requester (req/requester (d/db conn) req)]
           (res/transit-ok {:result {:auth (account/clientize requester)}}))))

  (context "/accounts" [] accounts/routes)
  (context "/checks" [] checks/routes)
  (context "/deposits" [] deposits/routes)
  (context "/income-file" [] income/routes)
  (context "/licenses" [] licenses/routes)
  (context "/metrics" [] metrics/routes)
  (context "/notes" [] notes/routes)
  (context "/properties" [] properties/routes)
  (context "/referrals" [] referrals/routes))
