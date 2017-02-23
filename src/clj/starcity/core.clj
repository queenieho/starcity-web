(ns starcity.core
  (:gen-class)
  (:require [starcity.environment]
            [starcity.server]
            [starcity.countries]
            [starcity.datomic]
            [starcity.log]
            [starcity.nrepl]
            [starcity.config]
            [starcity.config.stripe]
            [starcity.observers]
            [starcity.services.mailchimp]
            [starcity.services.mailgun]
            [starcity.scheduler]
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
    (-> (mount/swap {#'starcity.environment/environment (:env options)})
        mount/start)))
