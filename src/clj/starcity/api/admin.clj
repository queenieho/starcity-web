(ns starcity.api.admin
  (:require [starcity.api.admin.applications :as applications]
            [compojure.core :refer [context defroutes GET POST]]
            [starcity.api.common :refer :all]))

(defroutes routes
  (GET "/applications" [] applications/fetch-applications)
  (GET "/applications/:application-id" [] applications/fetch-application)

  (GET "/income-file/:file-id" [] applications/fetch-income-file))
