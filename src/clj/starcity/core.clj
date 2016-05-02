(ns starcity.core
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [starcity.server :as server]
            [starcity.datomic :as datomic]
            [starcity.logger :as logger]
            [starcity.nrepl :as nrepl]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

(defn system [config]
  (let [{:keys [web-port db nrepl-port]} config]
    (logger/prod-setup)
    (component/system-map
     :datomic (datomic/datomic db)
     :nrepl (nrepl/nrepl-server nrepl-port)
     :webserver (component/using
                 (server/server web-port)
                 [:datomic]))))

(def config
  {:web-port   8080
   :nrepl-port 7889
   :db         {:uri        "datomic:mem://localhost:4334/starcity"
                :schema-dir "datomic/schemas"
                :seed-dir   "datomic/seed"}})

(defn -main [& args]
  (component/start (system config)))
