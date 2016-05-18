(ns starcity.server
  (:require [ring.adapter.jetty :refer [run-jetty]] ; server
            [bidi.bidi :as bidi]                    ; routing
            ;; middleware
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [buddy.auth.middleware :refer [wrap-authentication
                                           wrap-authorization]]
            [starcity.middleware :refer [wrap-components
                                         wrap-environment
                                         wrap-logging
                                         auth-backend
                                         wrap-exception-handling]]
            ;; pages
            [starcity.pages.landing :as landing]
            [starcity.pages.auth :as auth]
            [starcity.pages.dashboard :as dashboard]
            [starcity.pages.rental-application :as rental]
            ;; util
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Helpers

;; =============================================================================
;; Routes

(def routes
  ["/" {""       :index
        "login"  :login
        "logout" :logout
        "me"     {true :dashboard}

        true     :index ; catch-all
        }])

;; =============================================================================
;; Handlers

(defn handler [{:keys [uri request-method] :as req}]
  (let [match (bidi/match-route routes uri :request-method request-method)]
    (case (:handler match)
      :index     (landing/handle req)
      :login     (auth/handle-login req)
      :logout    (auth/handle-logout req)
      :dashboard (dashboard/handle req)
      req)))


(defn app-handler [env datomic]
  (-> handler
      (wrap-logging)
      (wrap-environment env)
      (wrap-components :db datomic)
      (wrap-authorization auth-backend)
      (wrap-authentication auth-backend)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-session)
      (wrap-resource "public")
      (wrap-exception-handling)))

;; =============================================================================
;; WebServer

(defrecord WebServer [port env datomic]
  component/Lifecycle
  (start [component]
    (debugf "Starting server on port %d" port)
    (assoc component :server (run-jetty (app-handler env datomic)
                                        {:port port :join? false})))
  (stop [component]
    (debug "Shutting down server")
    (.stop (:server component))
    component))

(defn server
  ([port]
   (server port :production))
  ([port env]
   (map->WebServer {:port port :env env})))
