(ns starcity.datomic.migration-old
  (:require [starcity.datomic.conformity :as c]
            [starcity.environment :refer [environment]]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [me.raynes.fs :as fs]
            [clojure.string :as str]
            [taoensso.timbre :refer [info infof]]
            [clojure.java.io :as io]))

;; NOTE: Consider parameterizing the migration directory from project
;; config, thus allowing for a dir of env-specific migrations

;; TODO: split migration-name on `.' and create subdirs

;; =============================================================================
;; Creating Migrations
;; =============================================================================

;; =============================================================================
;; Helpers

(def ^:private migration-formatter
  (f/formatter "YYYY-MM-dd-HH-mm-ss"))

(def ^:private migration-path
  "src/clj/starcity/datomic/migrations")

(defn- migration-template
  [migration-name env]
  (let [env-str       (if env (str "\n:env " env) "")
        timestamp-str (str "Created on " (f/unparse migration-formatter (t/now)) ".")]
    (format "(ns starcity.datomic.migrations.%s\n  \"%s\")\n\n(def migration\n  [])\n\n(def norms\n  {:starcity/%s {:txes [migration]%s}})"
            migration-name timestamp-str migration-name env-str)))

(defn- generate-migration
  [migration-name env]
  (let [filename (-> (str migration-name ".clj")
                     (str/replace "-" "_"))
        template (migration-template migration-name env)]
    [filename template]))

;; =============================================================================
;; API

(defn new-migration!
  [migration-name & {:keys [env]}]
  (let [env                (when env (if (true? env) environment env))
        [filename content] (generate-migration migration-name env)
        path               (format "%s/%s" migration-path filename)]
    (when-not (fs/exists? migration-path)
      (fs/mkdir migration-path))
    (if (fs/exists? path)
      (throw (Exception. (format "A migration with that name already exists! Not overwriting '%s'!" filename)))
      (do
        (spit path content)
        path))))
