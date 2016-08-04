(ns starcity.server
  (:require [ring.adapter.jetty :refer [run-jetty]] ; server
            [org.httpkit.server :refer [run-server]]
            ;; middleware
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.json :refer [wrap-json-params
                                          wrap-json-response]]
            [buddy.auth.middleware :refer [wrap-authentication
                                           wrap-authorization]]
            [starcity.auth :refer [auth-backend]]
            [starcity.middleware :refer [wrap-logging
                                         wrap-exception-handling]]
            [starcity.routes :refer [app-routes]]
            [starcity.services.slack :as slack]
            ;; util
            [mount.core :as mount :refer [defstate]]
            [starcity.config :refer [config]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Handler & Middleware

(def app-handler
  (-> app-routes
      (wrap-logging)
      (wrap-authorization auth-backend)
      (wrap-authentication auth-backend)
      (wrap-keyword-params)
      (wrap-nested-params)
      (wrap-json-params)
      (wrap-json-response)
      (wrap-params)
      (wrap-multipart-params)
      (wrap-resource "public")
      (wrap-session)
      (wrap-exception-handling)
      (wrap-content-type)))

;; =============================================================================
;; API

(defn- start-server
  [{:keys [port] :as conf}]
  (debugf "Starting server on port %d" port)
  (slack/webhook-request ":: *starting* webserver ::")
  (run-server app-handler {:port port}))

(defn- stop-server
  [server]
  (debug "Shutting down web server")
  (slack/webhook-request ":: *stopping* webserver ::")
  (server))

(defstate web-server
  :start (start-server (:webserver config))
  :stop  (stop-server web-server))
