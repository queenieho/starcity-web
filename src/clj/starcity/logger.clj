(ns starcity.logger
  (:require [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre :as timbre :refer [merge-config! set-level!]]
            [starcity.config :refer [config]]
            [mount.core :as mount :refer [defstate]]))

(defn- setup-logger
  [{:keys [level logfile] :as conf}]
  (merge-config!
   {:level     level
    :appenders {:spit (appenders/spit-appender {:fname logfile})}}))

(defstate logger
  :start (setup-logger (:logger config)))
