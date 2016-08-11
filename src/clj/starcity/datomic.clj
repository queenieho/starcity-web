(ns starcity.datomic
  (:require [datomic.api :as d]
            [mount.core :as mount :refer [defstate]]
            [starcity.environment :as env]
            [starcity.config :as config]
            [starcity.datomic.conformity :as c]
            [starcity.datomic.migrations :refer [migration-norms]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- run-migrations
  "Given a database connection, run database migrations."
  [conn]
  (let [norms (migration-norms conn)]
    (info "Ensuring the following conforms: " (keys norms))
    (c/ensure-conforms conn norms)))

;; TODO: Include information about the connection URI, but without the
;; username/password included
(defn- new-connection [{:keys [uri]}]
  (info "Establishing Datomic Connection!")
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (run-migrations conn)
    conn))

(defn- disconnect [{:keys [uri]} conn]
  (info "Releasing Datomic connection!")
  (.release conn))

;; =============================================================================
;; API
;; =============================================================================

(defstate conn
  :start (new-connection config/datomic)
  :stop  (disconnect config/datomic conn))

(defn tempid []
  (d/tempid (:partition config/datomic)))
