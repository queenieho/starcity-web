(ns starcity.server
  (:require [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [mount.core :as mount :refer [defstate]]
            [org.httpkit.server :refer [run-server]]
            [plumbing.core :refer [assoc-when]]
            [ring.middleware
             [content-type :refer [wrap-content-type]]
             [keyword-params :refer [wrap-keyword-params]]
             [multipart-params :refer [wrap-multipart-params]]
             [nested-params :refer [wrap-nested-params]]
             [not-modified :refer [wrap-not-modified]]
             [params :refer [wrap-params]]
             [resource :refer [wrap-resource]]
             [session :refer [wrap-session]]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.session.datomic :refer [datomic-store]]
            [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :as strategies]
            [starcity
             [datomic :refer [conn]]
             [auth :refer [auth-backend]]
             [config :refer [config]]
             [routes :refer [app-routes]]]
            [taoensso.timbre :as t]
            [starcity.environment :as env]
            [clojure.string :as string]))

;; =============================================================================
;; Middleware
;; =============================================================================

;; TODO: This doesn't fail well when we're dealing with an api request
(defn wrap-exception-handling
  [handler]
  (fn [{:keys [identity uri request-method remote-addr] :as req}]
    (try
      (handler req)
      (catch Exception e
        (do
          (t/error e ::unhandled (assoc-when {:uri         uri
                                              :method      request-method
                                              :remote-addr remote-addr}
                                             :user (:account/email identity)))
          {:status 500 :body "Unexpected server error!"})))))

(defn wrap-logging
  "Middleware to log requests."
  [handler]
  (fn [{:keys [uri request-method identity remote-addr] :as req}]
    (when-not (or (= uri "/favicon.ico")
                  (string/starts-with? uri "/assets")
                  (string/starts-with? uri "/bundles"))
      (t/trace ::request (assoc-when {:uri         uri
                                      :method      request-method
                                      :remote-addr remote-addr}
                                     :user (:account/email identity))))
    (handler req)))

(defn- datomic-session-store [conn]
  (letfn [(session->entity [value]
            {:session/value   value
             :session/account (-> value :identity :db/id)})]
    (datomic-store conn :session->entity session->entity)))

;; =============================================================================
;; Ring Handler
;; =============================================================================

(def optimus-bundles
  ""
  {;;; Public Site JS
   "main.js"       ["/js/main.js"]

   ;; TODO: Phase these out...
   "bank-info.js"  ["/js/jquery.validate.js"
                    "/js/bank-info.js"]
   "pay-by-ach.js" ["/js/pay-by-ach.js"]

   ;;; CLJS apps
   "admin.js"      ["/js/cljs/admin.js"]
   "apply.js"      ["/js/cljs/apply.js"]
   "mars.js"       ["/js/cljs/mars.js"]
   "onboarding.js" ["/js/cljs/onboarding.js"]

   ;;; Styles
   "antd.css"   ["/assets/css/antd.css"]
   "public.css" ["/assets/css/public.css"]
   "styles.css" ["/assets/css/starcity.css"]})

(defn- assemble-assets []
  (concat
   (assets/load-bundles "public" optimus-bundles)
   (assets/load-assets "public" [#"/assets/img/*"])))

(defn app-handler [conn]
  (let [[optimize strategy]
        (if (env/is-development?)
          [optimizations/none strategies/serve-live-assets]
          [optimizations/all strategies/serve-frozen-assets])]
    (-> app-routes
        (optimus/wrap assemble-assets optimize strategy)
        (wrap-authorization auth-backend)
        (wrap-authentication auth-backend)
        (wrap-logging)
        (wrap-keyword-params)
        (wrap-nested-params)
        (wrap-restful-format)
        (wrap-params)
        (wrap-multipart-params)
        (wrap-resource "public")
        (wrap-session {:store (datomic-session-store conn)})
        (wrap-exception-handling)
        (wrap-content-type)
        (wrap-not-modified))))

;; =============================================================================
;; API
;; =============================================================================

(defn- start-server
  [{:keys [port] :as conf}]
  (t/info ::start {:port port})
  (run-server (app-handler conn) {:port port :max-body (* 20 1024 1024)}))

(defn- stop-server
  [server]
  (t/info ::stop)
  (server))

(defstate web-server
  :start (start-server (:webserver config))
  :stop  (stop-server web-server))
