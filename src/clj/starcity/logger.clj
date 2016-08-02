(ns starcity.logger
  (:require [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre.appenders.3rd-party.rolling :as rolling]
            [taoensso.timbre :as timbre :refer [merge-config! set-level!]]
            [starcity.environment :refer [environment]]
            [starcity.config :refer [config]]
            [mount.core :as mount :refer [defstate]]))

(defn- appenders-for-environment
  [{:keys [logfile]}]
  (let [default {:spit (appenders/spit-appender {:fname logfile})}]
    (get {:production {:rolling (rolling/rolling-appender {:path logfile})}
          :staging    {:rolling (rolling/rolling-appender {:path logfile})}}
         environment
         default)))

(defn- setup-logger
  [{:keys [level logfile] :as conf}]
  (timbre/infof "Setting up logger @ level %s, logging to %s" level logfile)
  (merge-config!
   {:level     level
    :appenders (appenders-for-environment conf)}))

(defstate logger
  :start (setup-logger (:logger config)))
