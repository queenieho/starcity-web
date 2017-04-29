(ns starcity.api
  (:require [buddy.auth.accessrules :refer [restrict]]
            [compojure.core :refer [context defroutes]]
            [starcity.api
             [admin :as admin]
             [apply :as apply]
             [mars :as mars]
             [onboarding :as onboarding]
             [orders :as orders]]
            [starcity.auth :refer [authenticated-user user-isa]]))

(defn- app-restrictions
  [required-role]
  {:handler  {:and [authenticated-user (user-isa required-role)]}
   :on-error (fn [_ _] {:status 403 :body "You are not authorized."})})

(def ^:private authenticated-restrictions
  {:handler  authenticated-user
   :on-error (fn [_ _] {:status 403 :body "You are not authorized."})})

(defroutes routes

  (context "/apply" []
    (restrict apply/routes (app-restrictions :account.role/applicant)))

  (context "/mars" []
    (restrict mars/routes (app-restrictions :account.role/member)))

  (context "/admin" []
    (restrict admin/routes (app-restrictions :account.role/admin)))

  (context "/onboarding" []
    (restrict onboarding/routes (app-restrictions :account.role/onboarding)))

  (context "/orders" []
    (restrict orders/routes authenticated-restrictions)))
