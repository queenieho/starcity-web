(ns user
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [com.stuartsierra.component :as component]
            [figwheel-sidecar.repl-api :as ra]
            [figwheel-sidecar.system :refer [fetch-config]]
            [starcity.logger :as logger]
            [starcity.server :as server]
            [starcity.datomic :as datomic]
            [starcity.config :refer [get-config]]
            [taoensso.timbre :as timbre]
            [schema.core :as s]))

(timbre/refer-timbre)

(s/set-fn-validation! true) ; turn on validation globally during development

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
  (let [{:keys [webserver datomic profile]} config]
    (logger/dev-setup)
    (component/system-map
     :datomic (datomic/datomic datomic)
     :webserver (component/using
                 (server/server webserver profile)
                 [:datomic])
     :figwheel (map->Figwheel (fetch-config)))))

;; =============================================================================
;; Reloaded Workflow

(def system nil)

(defn init []
  (alter-var-root #'system
                  (constantly (dev-system (get-config :development)))))

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
