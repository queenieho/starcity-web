(ns starcity.api
  (:require [buddy.auth.accessrules :refer [restrict]]
            [compojure.core :refer [context defroutes]]
            [customs.access :as access]
            [starcity.api
             [admin :as admin]
             [mars :as mars]
             [onboarding :as onboarding]
             [orders :as orders]]))

(defn- app-restrictions
  [required-role]
  {:handler  {:and [access/authenticated-user (access/user-isa required-role)]}
   :on-error (fn [_ _] {:status 403 :body "You are not authorized."})})

(def ^:private authenticated-restrictions
  {:handler  access/authenticated-user
   :on-error (fn [_ _] {:status 403 :body "You are not authorized."})})

(defroutes routes

  (context "/mars" []
    (restrict mars/routes (app-restrictions :account.role/member)))

  (context "/admin" []
    (restrict admin/routes (app-restrictions :account.role/admin)))

  (context "/onboarding" []
    (restrict onboarding/routes (app-restrictions :account.role/onboarding)))

  (context "/orders" []
    (restrict orders/routes authenticated-restrictions)))
