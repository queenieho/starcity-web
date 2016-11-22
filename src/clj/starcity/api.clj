(ns starcity.api
  (:require [compojure.core :refer [context defroutes GET POST]]
            [buddy.auth.accessrules :refer [restrict]]
            [starcity.auth :refer [user-isa authenticated-user]]
            [starcity.api
             [plaid :as plaid]
             [dashboard :as dashboard]
             [admin :as admin]
             [onboarding :as onboarding]
             [apply :as apply]
             [common :refer :all]]))

(defn- restrictions
  [required-role]
  {:handler  {:and [authenticated-user (user-isa required-role)]}
   :on-error (fn [_ _] {:status 403 :body "You are not authorized."})})

(defroutes routes
  (POST "/plaid/verify/income" [] plaid/verify-income)

  (context "/apply" []
    (restrict apply/routes (restrictions :account.role/applicant)))

  (context "/dashboard" []
    (restrict dashboard/routes (restrictions :account.role/member)))

  (context "/admin" []
    (restrict admin/routes (restrictions :account.role/admin)))

  (context "/onboarding" []
    (restrict onboarding/routes (restrictions :account.role/onboarding))))
