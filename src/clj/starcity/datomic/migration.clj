(ns starcity.datomic.migration
  (:require [clj-time.format :as f]
            [clj-time.core :as t]
            [me.raynes.fs :as fs]
            [clojure.string :as str]
            [starcity.datomic.conformity :as c]
            [starcity.environment :refer [environment]]
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

;; TODO: Not a fan of the following two Vars.
(def ^:private migration-dir-path
  "resources/migrations")

(def ^:private migration-dir-resource
  (io/file (io/resource "migrations")))

(defn- migration-template
  [migration-name env]
  (let [env-str (if env (str "\n:env " env) "")]
    (format "(ns migrations.%s)\n\n(def migration\n  [])\n\n(def norms\n  {:starcity/%s {:txes [migration]%s}})"
            migration-name migration-name env-str)))

(defn- generate-migration
  [migration-name include-timestamp? env]
  (let [timestamp      (f/unparse migration-formatter (t/now))
        migration-name (if include-timestamp?
                         (format "%s-%s" migration-name timestamp)
                         migration-name)
        filename       (-> (str migration-name ".clj")
                           (str/replace "-" "_"))
        template       (migration-template migration-name env)]
    [filename template]))

;; =============================================================================
;; API

(defn new-migration!
  [migration-name & {:keys [include-timestamp? env]
                     :or   {include-timestamp? true}}]
  (let [env                (when env (if (true? env) environment env))
        [filename content] (generate-migration migration-name include-timestamp? env)
        path               (format "%s/%s" migration-dir-path filename)]
    (do
      (when-not (fs/exists? migration-dir-path)
        (fs/mkdir migration-dir-path))
      (spit path content)
      path)))

;; =============================================================================
;; Running Migrations
;; =============================================================================

;; =============================================================================
;; Helpers

(defn- file-namespace
  [file]
  (let [stop (first (butlast (fs/split migration-dir-path)))]
    (fs/base-name
     (->> (fs/split file)
          (drop-while #(not= % stop))     ; get to the project
          (rest)
          (interpose ".")
          (apply str))
     true)))

(defn- file-ns->migration
  [file-ns]
  (-> file-ns
      (str/replace "_" "-")
      (str "/norms")
      (symbol)
      (eval)))

(defn- parse-migration
  "A norm-map may contain an :env keyword, which specifies the environment under
  which this migration should be run. The :env can take the form of a set of
  environments, or a single environment. This function includes the "
  [migration]
  (letfn [(-parse-env [without-norm [k {:keys [env] :as norm-map}]]
            (let [with-norm (assoc without-norm k norm-map)]
              (cond
                (nil? env) with-norm
                (set? env) (if (env environment) with-norm without-norm)
                :otherwise (if (= env environment) with-norm without-norm))))]
    (reduce -parse-env {} migration)))

(defn- file->norms
  "Given a migration file, produce a map of norms."
  [file]
  (let [file-ns (file-namespace file)
        ns'     (fs/path-ns file-ns)]
    (do
      (require ns')
      (-> file-ns
          (file-ns->migration)
          (parse-migration)))))

(defn- compile-migrations
  [dir]
  (reduce (fn [acc f]
            (if (fs/directory? f)
              (compile-migrations f)
              (let [norms (file->norms f)]
                (when-not (empty? norms)
                  (infof "Merging migrations from: %s" (fs/base-name f)))
                (merge acc norms))))
          {}
          (fs/list-dir dir)))

;; =============================================================================
;; API

(defn run-migrations
  "Given a database connection, run database migrations."
  [conn]
  (let [norms (compile-migrations migration-dir-resource)]
    (c/ensure-conforms conn norms)))
