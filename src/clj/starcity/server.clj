(ns starcity.server
  (:require [buddy.auth :as buddy]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [clojure.string :as string]
            [customs.access :as access]
            [mount.core :as mount :refer [defstate]]
            [optimus
             [assets :as assets]
             [optimizations :as optimizations]
             [prime :as optimus]
             [strategies :as strategies]]
            [org.httpkit.server :refer [run-server]]
            [plumbing.core :refer [assoc-when]]
            [ring.middleware
             [content-type :refer [wrap-content-type]]
             [format :refer [wrap-restful-format]]
             [keyword-params :refer [wrap-keyword-params]]
             [multipart-params :refer [wrap-multipart-params]]
             [nested-params :refer [wrap-nested-params]]
             [not-modified :refer [wrap-not-modified]]
             [params :refer [wrap-params]]
             [resource :refer [wrap-resource]]
             [session :refer [wrap-session]]]
            [ring.middleware.session.datomic :refer [datomic-store session->entity]]
            [ring.util.response :as response]
            [starcity
             [config :refer [config]]
             [datomic :refer [conn]]
             [environment :as env]
             [routes :refer [app-routes]]]
            [taoensso.timbre :as t]))

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

;; =============================================================================
;; Ring Handler
;; =============================================================================

(def optimus-bundles
  ""
  {;;; Public Site JS
   "main.js" ["/js/main.js"]
   "tour.js" ["/js/tour.js"]

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

(defn- unauthorized-handler
  "Default unauthorized handler."
  [{:keys [headers] :as request} metadata]
  (cond
    (buddy/authenticated? request) (-> (response/response "You are not authorized to view this page.")
                                       (response/content-type "text/html; charset=utf-8")
                                       (assoc :status 403))
    :else                          (let [current-url (:uri request)]
                                     ;; NOTE: Treat /application as a special case,
                                     ;; since it'll be triggered from the landing page
                                     ;; most frequently
                                     (if (= current-url "/apply")
                                       (response/redirect "/signup")
                                       (response/redirect (format "/login?next=%s" current-url))))))

(defn app-handler [conn]
  (let [[optimize strategy]
        (if (env/is-development?)
          [optimizations/none strategies/serve-live-assets]
          [optimizations/all strategies/serve-frozen-assets])]
    (-> app-routes
        (optimus/wrap assemble-assets optimize strategy)
        (wrap-authorization (access/auth-backend :unauthorized-handler unauthorized-handler))
        (wrap-authentication (access/auth-backend :unauthorized-handler unauthorized-handler))
        (wrap-logging)
        (wrap-keyword-params)
        (wrap-nested-params)
        (wrap-restful-format)
        (wrap-params)
        (wrap-multipart-params)
        (wrap-resource "public")
        (wrap-session {:store        (datomic-store conn :session->entity session->entity)
                       :cookie-attrs {:secure (not (env/is-development?))}})
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
