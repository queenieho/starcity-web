(ns starcity.server
  (:require [ring.util.response :refer [response file-response resource-response]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [bidi.bidi :as bidi]
            [com.stuartsierra.component :as component]
            [starcity.views.landing :refer [landing-page]]))

;; =============================================================================
;; Helpers

(defn html-response
  [response]
  (assoc response :headers {"Content-Type" "text/html; charset=utf-8"}))

(def ok (comp html-response response))

;; =============================================================================
;; Routes

(def routes
  ["/" {"" :index}])

;; =============================================================================
;; Handlers

(defn handler [req]
  (let [match (bidi/match-route routes (:uri req)
                                :request-method (:request-method req))]
    (case (:handler match)
      :index (ok (landing-page req))
      req)))

(defn app-handler []
  (-> handler
      (wrap-keyword-params)
      (wrap-params)
      (wrap-resource "public")))

(defn app-handler-dev []
  (fn [req]
    ((app-handler) req)))

;; =============================================================================
;; WebServer

(defrecord WebServer [port handler container]
  component/Lifecycle
  (start [component]
    (if container
      (let [req-handler (handler)
            container   (run-jetty req-handler {:port port :join? false})]
        (assoc component :container container))
      (assoc component :handler (handler))))
  (stop [component]
    (.stop container)))

(defn dev-server [port]
  (WebServer. port app-handler-dev true))

(defn prod-server []
  (WebServer. nil app-handler false))
