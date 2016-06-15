(ns starcity.core
  (:gen-class)
  (:require [starcity.server]
            [starcity.datomic]
            [starcity.logger]
            [starcity.nrepl]
            [starcity.config]
            [starcity.environment]
            [starcity.services.mailchimp]
            [starcity.services.mailgun]
            [mount.core :as mount]))

(defn -main [& args]
  (mount/start-with {#'starcity.environment/environment :production}))
