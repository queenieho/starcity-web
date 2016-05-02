(ns starcity.nrepl
  (:require [clojure.tools.nrepl.server :refer [start-server stop-server]]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre]))

(timbre/refer-timbre)

;; =============================================================================
;; Component

(defrecord ReplServer [port]
  component/Lifecycle
  (start [component]
    (debugf "Starting nREPL server on port %d" port)
    (assoc component :server (start-server :port port)))
  (stop [component]
    (stop-server (:server component))
    component))

(defn nrepl-server [port]
  (map->ReplServer {:port port}))
