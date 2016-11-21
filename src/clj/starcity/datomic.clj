(ns starcity.datomic
  (:require [datomic.api :as d]
            [mount.core :as mount :refer [defstate]]
            [starcity.environment :as env]
            [starcity.config.datomic :as config]
            [starcity.datomic.conformity :as c]
            [starcity.datomic
             [schema :as schema]
             [migrata :as migrata]
             [seed :as seed]]
            [starcity.log :as log]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- new-connection [{:keys [uri] :as conf}]
  (log/info ::connecting {:uri uri})
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (schema/install conn)
    (migrata/migrate conn)
    (when-not (env/is-production?) (seed/seed conn))
    conn))

(defn- disconnect [{:keys [uri]} conn]
  (log/info ::disconnecting {:uri uri})
  (.release conn))

;; =============================================================================
;; API
;; =============================================================================

(defstate conn
  :start (new-connection config/datomic)
  :stop  (disconnect config/datomic conn))

(defn tempid []
  (d/tempid config/partition))
