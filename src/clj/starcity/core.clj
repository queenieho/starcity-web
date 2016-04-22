(ns starcity.core
  (:require [com.stuartsierra.component :as component]
            [figwheel-sidecar.repl-api :as ra]
            [starcity.server :as server]
            [starcity.datomic :as datomic]))

(defrecord Figwheel []
  component/Lifecycle
  (start [config]
    (when-not (ra/figwheel-running?)
      (println "Starting figwheel!")
      (ra/start-figwheel! config))
    config)
  (stop [config]
    (println "Not stopping figwheel!")
    ;; Figwheel shouldn't get reset!
    ;; (ra/stop-figwheel!)
    config))

(defn dev-system [config]
  (let [{:keys [web-port db]} config]
    (component/system-map
     :datomic (datomic/datomic db)
     :webserver (component/using
                 (server/dev-server web-port)
                 [:datomic])
     :figwheel (map->Figwheel (figwheel-sidecar.system/fetch-config)))))
