(ns starcity.datomic
  (:require [datomic.api :as d]
            [mount.core :as mount :refer [defstate]]
            [starcity.config.datomic :as config]
            [starcity.datomic.schema :as schema]
            [taoensso.timbre :as timbre]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- new-connection [{:keys [uri] :as conf}]
  (timbre/info ::connecting {:uri uri})
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (schema/install conn)
    conn))

(defn- disconnect [{:keys [uri]} conn]
  (timbre/info ::disconnecting {:uri uri})
  (.release conn))

;; =============================================================================
;; API
;; =============================================================================

(defstate conn
  :start (new-connection config/datomic)
  :stop  (disconnect config/datomic conn))

(defn tempid []
  (d/tempid config/partition))
