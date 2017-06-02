(ns starcity.datomic
  (:require [datomic.api :as d]
            [mount.core :as mount :refer [defstate]]
            [starcity.config :as config :refer [config]]
            [starcity.datomic.seed :as seed]
            [blueprints.core :as db]
            [taoensso.timbre :as timbre]
            [clojure.core.async :as a]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- new-connection [uri]
  (timbre/info ::connecting {})
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (db/conform-db conn (config/datomic-part config))
    (seed/seed conn (:env (mount/args)))
    conn))

(defn- disconnect [uri conn]
  (timbre/info ::disconnecting {})
  (d/release conn))

(defn- install-report-queue [conn c]
  (a/thread
    (try
      (let [queue (d/tx-report-queue conn)]
        (while true
          (let [report (.take queue)]
            (a/>!! c report))))
      (catch Exception e
        (timbre/debug e "TX-REPORT-TAKE exception")
        (throw e)))))

;; =============================================================================
;; API
;; =============================================================================

(defstate conn
  :start (new-connection (config/datomic-uri config))
  :stop  (disconnect (config/datomic-uri config) conn))

(def ^:private buffer-size
  "Will likely want to make this configurable at some point."
  (Math/pow 2 14))

(defstate ^:private tx-report-ch
  :start (a/chan (a/sliding-buffer buffer-size))
  :stop (a/close! tx-report-ch))

;; From https://github.com/thegeez/gin/blob/master/src/gin/system/database_datomic.clj
(defstate ^:private tx-report-queue
  :start (install-report-queue conn tx-report-ch)
  :stop (d/remove-tx-report-queue conn))

(defstate listener :start (a/mult tx-report-ch))

(defn tempid []
  (d/tempid (config/datomic-part config)))
