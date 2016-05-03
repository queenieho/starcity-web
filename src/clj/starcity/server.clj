(ns starcity.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.util.response :as response]
            [bidi.bidi :as bidi]
            [com.stuartsierra.component :as component]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.auth.backends.session :refer [session-backend]]
            [starcity.pages.landing :as landing]
            [starcity.pages.auth :as auth]
            [starcity.pages.dashboard :as dashboard]
            [starcity.pages.rental-application :as rental]
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
        "apply"  :apply
        "me"     :dashboard

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
      :apply     (rental/handle req)
      req)))

(defn wrap-log
  "Middleware to log requests."
  [handler]
  (fn [{:keys [uri params request-method] :as req}]
    (when-not (= uri "/favicon.ico")
      (tracef "REQUEST :: uri -- %s :: params -- %s :: method -- %s"
             uri params request-method))
    (handler req)))

(defn wrap-components
  "Middleware to inject components into the request map."
  [handler & kvs]
  (assert (even? (count kvs)))
  (let [components (partition 2 kvs)]
    (fn [req]
      (handler (reduce (fn [acc [id c]]
                         (assoc acc id c))
                       req
                       components)))))

(defn unauthorized-handler
  [request metadata]
  (cond
    (authenticated? request) (-> (response/response "UNAUTHENTICATED")
                                 (response/content-type "text/html; charset=utf-8")
                                 (assoc :status 403))
    :else                    (let [current-url (:uri request)]
                               (response/redirect (format "/login?next=%s" current-url)))))

(def auth-backend
  (session-backend {:unauthorized-handler unauthorized-handler}))

(defn app-handler [datomic]
  (-> handler
      (wrap-log)
      (wrap-components :db datomic)
      (wrap-authorization auth-backend)
      (wrap-authentication auth-backend)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-session)
      (wrap-resource "public")))

;; =============================================================================
;; WebServer

(defrecord WebServer [port datomic]
  component/Lifecycle
  (start [component]
    (debugf "Starting server on port %d" port)
    (assoc component :server (run-jetty (app-handler datomic)
                                        {:port port :join? false})))
  (stop [component]
    (debug "Shutting down server")
    (.stop (:server component))
    component))

(defn server [port]
  (map->WebServer {:port port}))
