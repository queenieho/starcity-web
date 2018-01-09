(ns starcity.server
  (:require [buddy.auth :as buddy]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [clojure.string :as string]
            [com.akolov.enlive-reload :refer [wrap-enlive-reload]]
            [customs.access :as access]
            [mount.core :as mount :refer [defstate]]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.prime :as optimus]
            [optimus.strategies :as strategies]
            [org.httpkit.server :refer [run-server]]
            [plumbing.core :refer [assoc-when]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.datomic :refer [datomic-store session->entity]]
            [ring.util.response :as response]
            [starcity.config :as config :refer [config]]
            [starcity.datomic :refer [conn]]
            [starcity.routes :refer [app-routes]]
            [taoensso.timbre :as t]))

;; =============================================================================
;; Middleware
;; =============================================================================


(defn wrap-exception-handling
  [handler]
  (fn [{:keys [session uri request-method remote-addr] :as req}]
    (try
      (handler req)
      (catch Exception e
        (do
          (t/error e ::unhandled (assoc-when {:uri         uri
                                              :method      request-method
                                              :remote-addr remote-addr}
                                             :user (get-in session [:identity :account/email])))
          {:status 500 :body "Unexpected server error!"})))))


(defn wrap-logging
  "Middleware to log requests."
  [handler]
  (fn [{:keys [uri request-method session remote-addr] :as req}]
    (when-not (or (= uri "/favicon.ico")
                  (string/starts-with? uri "/assets")
                  (string/starts-with? uri "/bundles"))
      (t/info :web/request (assoc-when {:uri         uri
                                        :method      request-method
                                        :remote-addr remote-addr}
                                       :user (get-in session [:identity :account/email]))))
    (handler req)))


(defn wrap-reload-templates
  [handler]
  (let [wrapped (-> handler wrap-reload wrap-enlive-reload)]
    (fn [req]
     (if (config/is-development? config)
       (wrapped req)
       (handler req)))))


;; =============================================================================
;; Ring Handler
;; =============================================================================

(def optimus-bundles
  ""
  {;;; Public Site JS
   "main.js" ["/js/main.js"]
   "tour.js" ["/js/tour.js"]

   ;;; FloorPlan Styles
   "styles.js" ["/js/styles.js"]

   ;;; North-Beach Floorplan Drawing
   "north-beach.js" ["/js/north-beach/north-beach.js"]

   ;;; North-Beach Floorplan Data Files
   ;;;; Floor 1
   "nb-floor1.js" ["/js/north-beach/f1/nb-floor1.js"]
   "nb-1-outwall.js" ["/js/north-beach/f1/nb-1-outwall.js"]
   "nb-f1-deck.js" ["/js/north-beach/f1/nb-f1-deck.js"]
   "nb-f1-bed1.js" ["/js/north-beach/f1/nb-f1-bed1.js"]
   "nb-f1-laundry.js" ["/js/north-beach/f1/nb-f1-laundry.js"]
   "nb-f1-toi.js" ["/js/north-beach/f1/nb-f1-toi.js"]
   "nb-f1-bath.js" ["/js/north-beach/f1/nb-f1-bath.js"]
   "nb-f1-storage.js" ["/js/north-beach/f1/nb-f1-storage.js"]
   "nb-f1-bed2.js" ["/js/north-beach/f1/nb-f1-bed2.js"]
   "nb-f1-foyer.js" ["/js/north-beach/f1/nb-f1-foyer.js"]
   ;;;; Floor 2
   "nb-floor2.js" ["/js/north-beach/f2/nb-floor2.js"]
   "nb-f2-outwall.js" ["/js/north-beach/f2/nb-2-outwall.js"]
   "nb-f2-bed1.js" ["/js/north-beach/f2/nb-f2-bed1.js"]
   "nb-f2-bed2.js" ["/js/north-beach/f2/nb-f2-bed2.js"]
   "nb-f2-bed3.js" ["/js/north-beach/f2/nb-f2-bed3.js"]
   "nb-f2-shower.js" ["/js/north-beach/f2/nb-f2-shower.js"]
   "nb-f2-toilet.js" ["/js/north-beach/f2/nb-f2-toilet.js"]
   "nb-f2-bath.js" ["/js/north-beach/f2/nb-f2-bath.js"]
   "nb-f2-bed4.js" ["/js/north-beach/f2/nb-f2-bed4.js"]
   "nb-f2-bed5.js" ["/js/north-beach/f2/nb-f2-bed5.js"]
   ;;;; Floor 3
   "nb-floor3.js" ["/js/north-beach/f3/nb-floor3.js"]
   "nb-f3-outwall.js" ["/js/north-beach/f3/nb-3-outwall.js"]
   "nb-f3-bed1.js" ["/js/north-beach/f3/nb-f3-bed1.js"]
   "nb-f3-bed2.js" ["/js/north-beach/f3/nb-f3-bed2.js"]
   "nb-f3-bed3.js" ["/js/north-beach/f3/nb-f3-bed3.js"]
   "nb-f3-shower.js" ["/js/north-beach/f3/nb-f3-shower.js"]
   "nb-f3-toilet.js" ["/js/north-beach/f3/nb-f3-toilet.js"]
   "nb-f3-bath.js" ["/js/north-beach/f3/nb-f3-bath.js"]
   "nb-f3-bed4.js" ["/js/north-beach/f3/nb-f3-bed4.js"]
   "nb-f3-bed5.js" ["/js/north-beach/f3/nb-f3-bed5.js"]



   ;;; CLJS apps
   "admin.js"      ["/js/cljs/admin.js"]
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
                                     (response/redirect (format "/login?next=%s" current-url)))))

(defn app-handler [conn]
  (let [[optimize strategy]
        (if (config/is-development? config)
          [optimizations/none strategies/serve-live-assets]
          [optimizations/all strategies/serve-frozen-assets])]
    (-> app-routes
        (wrap-reload-templates)
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
                       :cookie-name  (config/session-name config)
                       :cookie-attrs {:secure (config/secure-sessions? config)
                                      :domain (config/session-domain config)}})
        (wrap-exception-handling)
        (wrap-content-type)
        (wrap-not-modified))))

;; =============================================================================
;; API
;; =============================================================================

(defn- start-server [port]
  (t/info ::start {:port port})
  (run-server (app-handler conn) {:port port :max-body (* 20 1024 1024)}))

(defn- stop-server [server]
  (t/info ::stop)
  (server))

(defstate web-server
  :start (start-server (config/webserver-port config))
  :stop  (stop-server web-server))
