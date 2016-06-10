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
            [starcity.middleware :refer [wrap-logging
                                         auth-backend
                                         wrap-exception-handling]]
            ;; pages
            [starcity.pages.landing :as landing]
            [starcity.pages.register :as register]
            [starcity.pages.auth :as auth]
            [starcity.pages.dashboard :as dashboard]
            [starcity.pages.util :refer [ok]]
            ;; util
            [mount.core :as mount :refer [defstate]]
            [starcity.config :refer [config]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Helpers

;; =============================================================================
;; Routes

(def routes
  ["/" {""         :index
        "register" :register
        "login"    :login
        "signup"   {""          :signup
                    "/activate" :signup/activate
                    "/complete" :signup/complete}
        "logout"   :logout
        "me"       {true :dashboard}

        true       :index ; catch-all
        }])

;; =============================================================================
;; Handlers

(defn handler [{:keys [uri request-method] :as req}]
  (let [match (bidi/match-route routes uri :request-method request-method)]
    (case (:handler match)
      :index           (landing/handle req)
      :register        (register/handle req)
      :login           (auth/handle-login req)
      :logout          (auth/handle-logout req)
      :signup          (auth/handle-signup req)
      :signup/activate (auth/handle-activation req)
      :signup/complete (auth/handle-signup-complete req)
      :dashboard       (dashboard/handle req)
      req)))

(def app-handler
  (-> handler
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
