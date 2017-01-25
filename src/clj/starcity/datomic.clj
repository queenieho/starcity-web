(ns starcity.datomic
  (:require [datomic.api :as d]
            [mount.core :as mount :refer [defstate]]
            [starcity.environment :as env]
            [starcity.config.datomic :as config]
            [starcity-db.core :as db]
            [taoensso.timbre :as timbre]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- new-connection [{:keys [uri] :as conf}]
  (timbre/info ::connecting {:uri uri})
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (db/conform-schema conn)
    (try
      (db/conform-migrations conn)
      (catch Throwable t
        ;; NOTE: This will always happen in development, as there will be
        ;; no data to migrate. That's ok. If it happens in
        ;; production...not ok.
        (when (env/is-production?)
          (timbre/error t "Error encountered while attempting to run migrations.")
          (throw t))))
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
