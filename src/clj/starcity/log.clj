(ns starcity.log
  (:require [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre.appenders.3rd-party.rolling :as rolling]
            [taoensso.timbre :as timbre :refer [merge-config! set-level!]]
            [starcity.environment :refer [environment]]
            [starcity.config :refer [config]]
            [mount.core :as mount :refer [defstate]]
            [clojure.string :as str]))

;; =============================================================================
;; Log Configuration
;; =============================================================================

(defn- appenders-for-environment
  [{:keys [logfile]}]
  (let [default {:spit (appenders/spit-appender {:fname logfile})}]
    (get {:production {:rolling (rolling/rolling-appender {:path logfile})}
          :staging    {:rolling (rolling/rolling-appender {:path logfile})}}
         environment
         default)))

(defn- setup-log
  [{:keys [level logfile] :as conf}]
  (timbre/infof "Setting up logger @ level %s, logging to %s" level logfile)
  (merge-config!
   {:level     level
    :appenders (appenders-for-environment conf)}))

;; =============================================================================
;; API
;; =============================================================================

(defstate logger :start (setup-log (:log config)))

;; =============================================================================
;; Request Logging

(def ^:private params-blacklist
  #{:password :password-1 :password-2})

(defn- unidentified-request
  [{:keys [remote-addr request-method uri] :as req}]
  (format "REQUEST - [%s] - %s - %s"
          (-> request-method name str/upper-case)
          remote-addr
          uri))

(defn- identified-request
  [{:keys [identity] :as req}]
  (format "%s - %s"
          (unidentified-request req)
          (format "%s:%s" (:db/id identity) (:account/email identity))))

(defn- log-for-identity
  [{:keys [identity] :as req}]
  (if (empty? identity)
    (unidentified-request req)
    (identified-request req)))

(defn- include-params
  [log-statement params]
  (format "%s - %s" log-statement params))

(defn request
  ([req]
   (request req nil))
  ([{:keys [params identity] :as req} error]
   (let [with-identity (log-for-identity req)
         log-statement (if (not-empty params)
                         (include-params with-identity (apply dissoc params params-blacklist))
                         with-identity)]
     (if error
       (timbre/error error log-statement)
       (timbre/info log-statement)))))
