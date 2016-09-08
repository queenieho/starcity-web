(ns starcity.api
  (:require [compojure.core :refer [context defroutes GET POST]]
            [buddy.auth.accessrules :refer [restrict]]
            [starcity.auth :refer [user-isa]]
            [starcity.api.plaid :as plaid]
            [starcity.api.dashboard :as dashboard]
            [starcity.api.admin :as admin]
            [starcity.api.onboarding :as onboarding]
            [starcity.api.common :refer :all]))

(defn- restrictions
  [required-role]
  {:handler  (user-isa required-role)
   :on-error (fn [_ _] {:status 403 :body "You are not authorized."})})

(defroutes routes
  (POST "/plaid/verify/income" [] plaid/verify-income)

  (context "/dashboard" []
           (restrict dashboard/routes (restrictions :account.role/tenant)))

  (context "/admin" []
           (restrict admin/routes (restrictions :account.role/admin)))

  (context "/onboarding" []
           (restrict onboarding/routes (restrictions :account.role/pending))))
