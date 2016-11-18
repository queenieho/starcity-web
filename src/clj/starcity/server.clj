(ns starcity.server
  (:require [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [mount.core :as mount :refer [defstate]]
            [org.httpkit.server :refer [run-server]]
            [plumbing.core :refer [assoc-when]]
            [ring.middleware
             [content-type :refer [wrap-content-type]]
             [json :refer [wrap-json-params wrap-json-response]]
             [keyword-params :refer [wrap-keyword-params]]
             [multipart-params :refer [wrap-multipart-params]]
             [nested-params :refer [wrap-nested-params]]
             [params :refer [wrap-params]]
             [resource :refer [wrap-resource]]
             [session :refer [wrap-session]]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [starcity
             [auth :refer [auth-backend]]
             [config :refer [config]]
             [log :as log]
             [routes :refer [app-routes]]]
            [starcity.views.error :as view]))

;; =============================================================================
;; Handler & Middleware

(defn wrap-exception-handling
  [handler]
  (fn [{:keys [identity uri request-method remote-addr] :as req}]
    (try
      (handler req)
      (catch Exception e
        (do
          (log/exception e ::unhandled (assoc-when {:uri         uri
                                                    :method      request-method
                                                    :remote-addr remote-addr}
                                                   :user (:account/email identity)))
          {:status 500 :body (view/error "Unexpected server error.")})))))

(defn wrap-logging
  "Middleware to log requests."
  [handler]
  (fn [{:keys [uri request-method identity remote-addr] :as req}]
    (when-not (= uri "/favicon.ico")
      (log/info ::request (assoc-when {:uri         uri
                                       :method      request-method
                                       :remote-addr remote-addr}
                                      :user (:account/email identity))))
    (handler req)))

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
  (log/debug ::start {:port port})
  (run-server app-handler {:port port :max-body (* 20 1024 1024)}))

(defn- stop-server
  [server]
  (log/debug ::stop)
  (server))

(defstate web-server
  :start (start-server (:webserver config))
  :stop  (stop-server web-server))
