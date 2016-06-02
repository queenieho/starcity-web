(ns starcity.core
  (:gen-class)
  (:require [starcity.server]
            [starcity.datomic]
            [starcity.logger]
            [starcity.nrepl]
            [starcity.config]
            [starcity.environment]
            [mount.core :as mount]))

(defn -main [& args]
  (mount/start-with {#'starcity.environment/environment :production}))
