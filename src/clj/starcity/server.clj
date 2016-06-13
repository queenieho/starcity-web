(ns starcity.server
  (:require [ring.adapter.jetty :refer [run-jetty]] ; server
            [org.httpkit.server :refer [run-server]]
            [bidi.bidi :as bidi]                    ; routing
            ;; middleware
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [buddy.auth.middleware :refer [wrap-authentication
                                           wrap-authorization]]
            [starcity.auth :refer [auth-backend]]
            [starcity.middleware :refer [wrap-logging
                                         wrap-exception-handling]]
            [starcity.routes :refer [routes]]
            ;; util
            [mount.core :as mount :refer [defstate]]
            [starcity.config :refer [config]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Handler & Middleware

(def app-handler
  (-> routes
      (wrap-logging)
      (wrap-authorization auth-backend)
      (wrap-authentication auth-backend)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-session)
      (wrap-resource "public")
      (wrap-exception-handling)))

;; =============================================================================
;; API

(defn- start-server
  [{:keys [port] :as conf}]
  (debugf "Starting server on port %d" port)
  (run-server app-handler {:port port}))

(defn- stop-server
  [server]
  (debug "Shutting down web server")
  (server))

(defstate web-server
  :start (start-server (:webserver config))
  :stop  (stop-server web-server))
