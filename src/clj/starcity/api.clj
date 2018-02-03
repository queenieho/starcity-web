(ns starcity.api
  (:require [buddy.auth.accessrules :refer [restrict]]
            [compojure.core :refer [context defroutes]]
            [customs.access :as access]
            [starcity.api.admin :as admin]))

(defn- app-restrictions
  [required-role]
  {:handler  {:and [access/authenticated-user (access/user-isa required-role)]}
   :on-error (fn [_ _] {:status 403 :body "You are not authorized."})})

(def ^:private authenticated-restrictions
  {:handler  access/authenticated-user
   :on-error (fn [_ _] {:status 403 :body "You are not authorized."})})

(defroutes routes

  (context "/admin" []
    (restrict admin/routes (app-restrictions :account.role/admin))))
