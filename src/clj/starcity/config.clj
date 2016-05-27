(ns starcity.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [me.raynes.fs :as fs]
            [taoensso.timbre :refer [warn info]]))

;; =============================================================================
;; Constants

(def ^:private +config-dir+ "config/")
(def ^:private +secrets-file+ "~/.secrets.edn")

;; =============================================================================
;; Helpers

(defn- config-file [filename]
  (str +config-dir+ filename))

(defn- config-for-profile [profile]
  (config-file (str (name profile) ".edn")))

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

;; =============================================================================
;; API

(defn get-config [profile]
  (assert (#{:production :development} profile)
          (format "Profile must be one of #{:production :development}, not %s!" profile))
  (let [defaults (read-config (config-file "config.edn"))]
    (merge-with merge
                defaults
                (read-config (config-for-profile profile))
                (read-secrets +secrets-file+))))
