(ns starcity.datomic
  (:require [datomic.api :as d]
            [cpath-clj.core :as cp]
            [taoensso.timbre :as timbre]
            [clojure.java.io :as io]
            [starcity.config :refer [config]]
            [starcity.datomic.util :refer [qes]]
            [mount.core :as mount :refer [defstate]])
  (:import datomic.Util))

(timbre/refer-timbre)

;; ============================================================================
;; Schema

(defn- all-attrs
  [conn]
  (->> (qes '[:find ?a :where [_ :db.install/attribute ?a]]
            (d/db conn))
       (reduce #(conj %1 (d/touch %2)) [])))

(defn- new-idents
  [conn new]
  (letfn [(exists? [attr]
            (not-empty
             (filter #(= (:db/ident attr) (:db/ident %)) (all-attrs conn))))]
    (remove exists? new)))

(defn- read-edn
  [dir]
  (let [resources (cp/resources dir)]
    (->> (for [[filename _] resources]
           (with-open [f (-> (str dir filename) io/resource io/reader)]
             (Util/readAll f)))
         (flatten))))

(defn- install-schema
  "Ensure that any newly added attrs get transacted when the system is started."
  [conn dir]
  (let [schemas (read-edn dir)
        idents  (new-idents conn schemas ;; (remove #(nil? (:db/valueType %)) schemas)
                            )]
    (when-not (empty? idents)
      (debugf "Creating new attrs with idents: %s" (pr-str (map :db/ident idents)))
      @(d/transact conn (vec idents)))))

(defn- seed-db [conn seed-dir]
  (when seed-dir
    (debugf "Seeding database from %s" seed-dir)
    (try
      @(d/transact conn (vec (read-edn seed-dir))) ; hacky, but works!
      (catch Exception e
        (warn "Exception encountered while seeding database!" e)))))

(defn- new-connection [{:keys [uri schema-dir seed-dir]}]
  (infof "Establishing Datomic Connection @ URI: %s" uri)
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (install-schema conn schema-dir)
    (when seed-dir
      (seed-db conn seed-dir))
    conn))

(defn- disconnect [{:keys [uri]} conn]
  (infof "Releasing Datomic connection @ %s" uri)
  (.release conn))

;; =============================================================================
;; API

(defstate conn
  :start (new-connection (:datomic config))
  :stop  (disconnect (:datomic config) conn))

(defstate partition
  :start (get-in config [:datomic :partition]))
