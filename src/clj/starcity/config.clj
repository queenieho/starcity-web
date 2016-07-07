(ns starcity.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [me.raynes.fs :as fs]
            [taoensso.timbre :refer [warn info]]
            [starcity.environment :refer [environment]]
            [mount.core :as mount :refer [defstate]]))

;; =============================================================================
;; Constants

(def ^:private +config-dir+ "config/")
(def ^:private +secrets-file+ "~/.starcity-web-secrets.edn")

;; =============================================================================
;; Helpers

(defn- config-file [filename]
  (str +config-dir+ filename))

(defn- config-for-environment [environment]
  (config-file (str (name environment) ".edn")))

(defn- read-config [filename]
  (-> filename io/resource slurp edn/read-string))

(defn- read-secrets [secrets-file]
  (let [filename (fs/expand-home secrets-file)
        f        (fs/file filename)]
    (info "Attempting to read secrets from file:" filename)
    (try
      (edn/read-string (slurp f))
      (catch Exception e
        (warn "Exception encountered while attempting to read secrets file! Does it exist?" e)
        {}))))

(defn- load-config [environment]
  (assert (#{:production :development :staging} environment)
          (format "Environment must be one of #{:production :development :staging}, not %s!" environment))
  (let [defaults (read-config (config-file "config.edn"))]
    ;; TODO: deep-merge-with
    (-> (merge-with merge
                    defaults
                    (read-secrets +secrets-file+)
                    (read-config (config-for-environment environment)))
        (assoc :environment environment))))

;; =============================================================================
;; API

(defstate config :start (load-config environment))

;; TODO: Don't care for this too much. How to make more idiomatic?
(defn datomic-partition []
  (get-in config [:datomic :partition]))
