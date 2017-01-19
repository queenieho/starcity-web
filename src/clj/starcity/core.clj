(ns starcity.core
  (:gen-class)
  (:require [starcity.server]
            [starcity.seeder]
            [starcity.datomic]
            [starcity.log]
            [starcity.nrepl]
            [starcity.config]
            [starcity.environment]
            [starcity.services.mailchimp]
            [starcity.services.mailgun]
            [starcity.scheduler]
            [starcity.events.observers]
            [clojure.tools.cli :refer [parse-opts]]
            [mount.core :as mount]))


(def cli-options
  [["-e" "--environment ENVIRONMENT" "The environment to start the server in."
    :id :env
    :default :production
    :parse-fn keyword
    :validate [#{:production :development :staging} "Must be one of #{production, staging, development}"]]])

(defn- exit [status msg]
  (System/exit status))

(defn -main [& args]
  (let [{:keys [options errors]} (parse-opts args cli-options)]
    (when errors
      (exit 1 (clojure.string/join "\n" errors)))
    (mount/start-with {#'starcity.environment/environment (:env options)})))
