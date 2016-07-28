(ns starcity.datomic
  (:require [datomic.api :as d]
            [mount.core :as mount :refer [defstate]]
            [starcity.config :refer [config]]
            [starcity.datomic.conformity :as c]
            [starcity.datomic.migrations.initial :refer [initial-migration]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- run-migrations
  "Given a database connection, run database migrations."
  [conn]
  (let [norms (initial-migration)]
    (c/ensure-conforms conn norms)))

(defn- new-connection [{:keys [uri schema-dir seed-dir]}]
  (infof "Establishing Datomic Connection @ URI: %s" uri)
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (run-migrations conn)
    conn))

(defn- disconnect [{:keys [uri]} conn]
  (infof "Releasing Datomic connection @ %s" uri)
  (.release conn))

;; =============================================================================
;; API
;; =============================================================================

(defstate conn
  :start (new-connection (:datomic config))
  :stop  (disconnect (:datomic config) conn))
