(ns starcity.logger
  (:require [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre :as timbre :refer [merge-config! set-level!]]))

(timbre/refer-timbre)

(defn dev-setup
  []
  (merge-config!
   {:appenders
    {:spit (appenders/spit-appender {:fname "logs/server.log"})}}))

(defn prod-setup
  []
  (merge-config!
   {:appenders
    {:spit (appenders/spit-appender {:fname "/var/log/starcity/server.log"})}}))
