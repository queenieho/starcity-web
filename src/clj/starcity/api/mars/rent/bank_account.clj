(ns starcity.api.mars.rent.bank-account
  (:require [compojure.core :refer [context defroutes GET]]
            [datomic.api :as d]
            [starcity.api.mars.rent.bank-account.setup :as setup]
            [starcity.datomic :refer [conn]]
            [starcity.models.rent :as rent]
            [starcity.util.request :as req]
            [starcity.util.response :as res]))

(defn bank-account-handler [req]
  (let [requester (req/requester (d/db conn) req)]
    (res/json-ok {:bank-account (rent/bank-account (d/db conn) requester)})))

(defroutes routes
  (GET "/" [] bank-account-handler)
  (context "/setup" [] setup/routes))
