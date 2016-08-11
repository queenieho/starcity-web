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
            [ring.middleware.session.cookie :refer [cookie-store]]
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
            [starcity.environment :refer [environment]]
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
      (wrap-session {:store (cookie-store {:key (get-in config [:session :key])})})
      (wrap-exception-handling)
      (wrap-content-type)))

;; =============================================================================
;; API

(defn- start-server
  [{:keys [port] :as conf}]
  (debugf "Starting server on port %d" port)
  (when-not (= :development environment)
    (slack/send-message "starting webserver"))
  (run-server app-handler {:port port :max-body (* 20 1024 1024)}))

(defn- stop-server
  [server]
  (debug "Shutting down web server")
  (when-not (= :development environment)
    (slack/send-message "stopping webserver"))
  (server))

(defstate web-server
  :start (start-server (:webserver config))
  :stop  (stop-server web-server))
