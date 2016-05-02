(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [com.stuartsierra.component :as component]
            [figwheel-sidecar.repl-api :as ra]
            [starcity.logger :as logger]
            [starcity.server :as server]
            [starcity.datomic :as datomic]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Figwheel

(defrecord Figwheel []
  component/Lifecycle
  (start [config]
    (when-not (ra/figwheel-running?)
      (debug "Starting Figwheel server")
      (ra/start-figwheel! config))
    config)
  (stop [config]
    (debug "Not stopping Figwheel server!")
    ;; Figwheel shouldn't get reset!
    ;; (ra/stop-figwheel!)
    config))

;; =============================================================================
;; Development System

(defn dev-system [config]
  (let [{:keys [web-port db]} config]
    (logger/dev-setup)
    (component/system-map
     :datomic (datomic/datomic db)
     :webserver (component/using
                 (server/server web-port)
                 [:datomic])
     :figwheel (map->Figwheel (figwheel-sidecar.system/fetch-config)))))

(def config
  {:web-port 8080
   :db       {:uri        "datomic:mem://localhost:4334/starcity"
              :schema-dir "datomic/schemas"
              :seed-dir   "datomic/seed"}})

;; =============================================================================
;; Reloaded Workflow

(def system nil)

(defn init []
  (alter-var-root #'system
                  (constantly (dev-system config))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (refresh :after 'user/go))

(defn cljs-repl []
  (ra/cljs-repl))
