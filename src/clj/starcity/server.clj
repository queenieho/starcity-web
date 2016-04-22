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
  ["/" [["" :index]
        [true :index]]])

;; =============================================================================
;; Handlers

(defn handler [req]
  (let [match (bidi/match-route routes (:uri req)
                                :request-method (:request-method req))]
    (case (:handler match)
      :index (ok (landing-page req))
      req)))

(defn wrap-handler
  [& components]
  (fn [req]
    (handler (reduce (fn [acc [id c]]
                       (assoc acc id c))
                     req
                     components))))

(defn app-handler [datomic]
  (-> (wrap-handler
       [:datomic datomic])
      (wrap-keyword-params)
      (wrap-params)
      (wrap-resource "public")))

(defn app-handler-dev [datomic]
  (fn [req]
    ((app-handler datomic) req)))

;; =============================================================================
;; WebServer

(defrecord WebServer [port handler container datomic]
  component/Lifecycle
  (start [component]
    (if container
      (let [req-handler (handler datomic)
            container   (run-jetty req-handler {:port port :join? false})]
        (assoc component :container container))
      (assoc component :handler (handler datomic))))
  (stop [component]
    (.stop container)))

(defn dev-server [port]
  (WebServer. port app-handler-dev true nil))

(defn prod-server []
  (WebServer. nil app-handler false nil))
