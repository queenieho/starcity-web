(ns starcity.core
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [starcity.server :as server]
            [starcity.datomic :as datomic]
            [starcity.logger :as logger]
            [starcity.nrepl :as nrepl]
            [starcity.config :refer [config]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn system [config]
  (let [{:keys [webserver datomic nrepl profile]} config]
    (logger/prod-setup)
    (component/system-map
     :datomic (datomic/datomic datomic)
     :nrepl (nrepl/nrepl-server nrepl)
     :webserver (component/using
                 (server/server webserver profile)
                 [:datomic]))))

(defn -main [& args]
  (component/start (system (config :production))))
