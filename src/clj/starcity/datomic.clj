(ns starcity.datomic
  (:require [datomic.api :as d]
            [mount.core :as mount :refer [defstate]]
            [starcity.config :refer [config]]
            [starcity.datomic.conformity :as c]
            [starcity.datomic.migrations
             [initial :refer [initial-migration]]
             [properties-schema-8-2-16 :refer [properties-schema-8-2-16]]]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- run-migrations
  "Given a database connection, run database migrations."
  [conn]
  (let [norms (merge (initial-migration)
                     (properties-schema-8-2-16))]
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
  :start (new-connection (:datomic config))
  :stop  (disconnect (:datomic config) conn))
