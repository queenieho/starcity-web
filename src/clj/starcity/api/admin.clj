(ns starcity.api.admin
  (:require [starcity.api.admin.applications :as applications]
            [starcity.api.common :as api]
            [compojure.core :refer [context defroutes GET POST]]
            [starcity.util :refer [str->int]]))

(defroutes routes
  (GET "/applications" []
       (fn [_] (applications/fetch-applications)))

  (GET "/applications/:application-id" [application-id]
       (fn [_] (applications/fetch-application (str->int application-id))))

  (POST "/applications/:application-id/approve" [application-id]
        (fn [{:keys [params] :as req}]
          (let [{:keys [email-content deposit-amount community-id]} params]
            (applications/approve (str->int application-id)
                                  (api/account-id req)
                                  community-id
                                  (str->int deposit-amount)
                                  email-content))))

  (GET "/income-file/:file-id" [file-id]
       (fn [_] (applications/fetch-income-file (str->int file-id)))))
