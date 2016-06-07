(ns starcity.config
  (:require [immuconf.config :as conf]
            [starcity.environment :refer [environment]]
            [me.raynes.fs :as fs]
            [mount.core :as mount :refer [defstate]]
            [taoensso.timbre :refer [warn info]]))


;; =============================================================================
;; Constants

(def ^:private +config-dir+ "resources/config/")

;; =============================================================================
;; Helpers

(defn- config-file [filename]
  (str +config-dir+ filename))

(defn- config-for-environment [environment]
  (config-file (str (name environment) ".edn")))

(defn- load-config [environment]
  (assert (#{:production :development} environment)
          (format "Environment must be one of #{:production :development}, not %s!" environment))
  (conf/load "resources/config/config.edn"
             (config-for-environment environment)
             (fs/expand-home "~/.starcity-web-secrets.edn")))

;; =============================================================================
;; API

(defstate config :start (load-config environment))
