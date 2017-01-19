(ns starcity.log
  (:require [cheshire.core :as json]
            [clojure.spec :as s]
            [mount.core :as mount :refer [defstate]]
            [starcity
             [config :refer [config]]
             [environment :refer [environment]]]
            [taoensso.timbre :as timbre :refer [merge-config!]]
            [taoensso.timbre.appenders.3rd-party.rolling :as rolling]
            [taoensso.timbre.appenders.core :as appenders]))

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

(s/def ::event-vargs
  (s/cat :event keyword?
         :params map?))

(defn- event-vargs
  [data event params]
  (try
    (assoc data :vargs
           [(-> {:event event}
                (merge (when-let [err (:?err data)] {:error-data (or (ex-data err) :none)})
                       params)
                json/generate-string)])
    (catch Throwable t
      (timbre/warn t "Error encountered while attempting to encode vargs.")
      data)))

(defn- wrap-event-format
  "Middleware that transforms the user's log input into a JSON
  string with an `event` key. This is used to make search effective in LogDNA.

  Only applies when timbre is called with input of the form:

  (timbre/info ::event {:map :of-data})"
  [{:keys [vargs] :as data}]
  (if (s/valid? ::event-vargs vargs)
    (let [{:keys [event params]} (s/conform ::event-vargs vargs)]
      (event-vargs data event params))
    data))

(defn- setup-logger
  [{:keys [level logfile] :as conf}]
  (timbre/debug ::start {:level level :file logfile})
  (merge-config!
   {:level      level
    :middleware [wrap-event-format]
    :appenders  (appenders-for-environment conf)}))

(defstate logger :start (setup-logger (:log config)))

(comment
  (timbre/info (ex-info "Yikes!" {:data 42}) ::an-event {:message "A message."})

  )
