(ns starcity.core
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [starcity.server :as server]
            [starcity.datomic :as datomic]
            [starcity.logger :as logger]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn system [config]
  (let [{:keys [web-port db]} config]
    (logger/prod-setup)
    (component/system-map
     :datomic (datomic/datomic db)
     :webserver (component/using
                 (server/server web-port)
                 [:datomic]))))

(def config
  {:web-port 8080
   :db       {:uri        "datomic:mem://localhost:4334/starcity"
              :schema-dir "resources/datomic/schemas"}})

(defn -main [& args]
  (component/start (system config)))
