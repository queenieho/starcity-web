(ns starcity.server
  (:require [ring.util.response :refer [response file-response resource-response]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [bidi.bidi :as bidi]
            [com.stuartsierra.component :as component]
            [starcity.views.landing :refer [landing-page]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Helpers

(defn html-response
  [response]
  (assoc response :headers {"Content-Type" "text/html; charset=utf-8"}))

(def ok (comp html-response response))

;; =============================================================================
;; Routes

(def routes
  ["/" [["" :index]
        [true :index]]])

;; =============================================================================
;; Handlers

(defn handler [req]
  (let [match (bidi/match-route routes (:uri req)
                                :request-method (:request-method req))]
    (info "RECEIVED REQUEST: " req)
    (case (:handler match)
      :index (ok (landing-page req))
      req)))

(defn wrap-handler
  [& kvs]
  (assert (even? (count kvs)))
  (fn [req]
    (handler (reduce (fn [acc [id c]]
                       (assoc acc id c))
                     req
                     (partition 2 kvs)))))

(defn app-handler [datomic]
  (-> (wrap-handler
       :datomic datomic)
      (wrap-keyword-params)
      (wrap-params)
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
