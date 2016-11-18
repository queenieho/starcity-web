(ns starcity.log
  (:require [clojure.spec :as s]
            [mount.core :as mount :refer [defstate]]
            [starcity
             [config :refer [config]]
             [environment :refer [environment]]]
            [taoensso.timbre :as timbre :refer [merge-config!]]
            [taoensso.timbre.appenders.3rd-party.rolling :as rolling]
            [taoensso.timbre.appenders.core :as appenders]
            [cheshire.core :as json]))

;; =============================================================================
;; Logging Helpers
;; =============================================================================

;; NOTE: There's some efficiency loss here because we're using functions rather
;; than macros like timbre does. We're also using a limited subset of the
;; functionality afforded by timbre. A better solution may be to write a custom
;; output-fn for the appenders.

(defn- parse-data
  [event data]
  (-> (if (nil? data)
        {:event event}
        (assoc data :event event))
      (json/generate-string)))

(defn trace [event & [data]]
  (timbre/trace (parse-data event data)))

(defn debug [event & [data]]
  (timbre/debug (parse-data event data)))

(defn info [event & [data]]
  (timbre/info (parse-data event data)))

(defn warn [event & [data]]
  (timbre/warn (parse-data event data)))

(defn error [event & [data]]
  (timbre/error (parse-data event data)))

(defn fatal [event & [data]]
  (timbre/fatal (parse-data event data)))

(defn report [event & [data]]
  (timbre/report (parse-data event data)))

(defn exception [e event & [data]]
  (timbre/error e (parse-data event (merge data (ex-data e)))))

;; =============================================================================
;; Configuration
;; =============================================================================

(defn- appenders-for-environment
  [{:keys [logfile]}]
  (let [default {:spit (appenders/spit-appender {:fname logfile})}]
    (get {:production {:rolling (rolling/rolling-appender {:path logfile})}
          :staging    {:rolling (rolling/rolling-appender {:path logfile})}}
         environment
         default)))

(defn- setup-logger
  [{:keys [level logfile] :as conf}]
  (debug ::start {:level level :file logfile})
  (merge-config!
   {:level     level
    :appenders (appenders-for-environment conf)}))

(defstate logger :start (setup-logger (:log config)))
