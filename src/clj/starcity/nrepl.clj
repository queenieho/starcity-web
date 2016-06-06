(ns starcity.nrepl
  (:require [clojure.tools.nrepl.server :refer [start-server stop-server]]
            [starcity.config :refer [config]]
            [mount.core :as mount :refer [defstate]]
            [taoensso.timbre :refer [debugf]]))

;; =============================================================================
;; API

(defn- start-nrepl [{:keys [port]}]
  (debugf "Starting nREPL server on port %d" port)
  (start-server :port port))

(defstate nrepl
  :start (start-nrepl (:nrepl config))
  :stop  (stop-server nrepl))
