(ns starcity.services.slack
  (:refer-clojure :exclude [send])
  (:require [cheshire.core :as json]
            [clojure.core.async :refer [chan put!]]
            [clojure.string :as str]
            [org.httpkit.client :as http]
            [starcity.config.slack :as config]
            [starcity.environment :refer [is-development?]]
            [taoensso.timbre :as timbre]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private default-channel
  "#webserver")

(defn- assoc-channel-when
  "Assoc the channel into opts when channel is non-nil.

  If we're in development, override the channel with the `default-channel`."
  [opts channel]
  (if channel
    (if (is-development?)
      (assoc opts :channel default-channel)
      (assoc opts :channel channel))
    opts))

(defn- assoc-channel
  "Assoc the `channel` into `params`.

  If we're in development, override the channel with the `default-channel`."
  [opts channel]
  (if (is-development?)
    (assoc opts :channel default-channel)
    (assoc opts :channel channel)))

;; =============================================================================
;; API
;; =============================================================================

(defn ^{:deprecated "1.2.0"} send-message
  [text & {:keys [channel cb opts] :or {cb identity, opts {}}}]
  (http/post config/webhook-url
             {:headers {"Content-Type" "application/json"}
              :body    (json/generate-string (-> {:text     text
                                                  :username config/username}
                                                 (assoc-channel-when channel)
                                                 (merge opts)))}
             cb))

(defn ^{:deprecated "1.2.0"} rich-message
  [title text & {:keys [channel cb opts] :or {cb identity, opts {}}}]
  (let [opts     (assoc opts :fallback (or (:fallback opts) text))
        payload  (-> {:username    config/username
                      :attachments [(merge {:text text :title title} opts)]}
                     (assoc-channel-when channel))]
    (http/post config/webhook-url
               {:headers {"Content-Type" "application/json"}
                :body    (json/generate-string payload)}
               cb)))

;; =============================================================================
;; Build Message

(defn- ->channel [s]
  (if (str/starts-with? s "#") s (str "#" s)))

(defn- log-result
  [{:keys [error] :as res}]
  (if error
    (timbre/error error ::sent)
    (timbre/trace ::sent))
  res)

(defn send
  [{:keys [channel username] :or {channel default-channel}} msg]
  (let [out-c      (chan 1)
        msg-params (-> {:username (or username config/username)}
                       (assoc-channel (->channel channel)))]
    (http/post config/webhook-url
               {:keepalive 30000
                :headers   {"Content-Type" "application/json"}
                :body      (json/generate-string (merge msg msg-params))}
               (fn [res]
                 (put! out-c (log-result res))))
    out-c))

;;; Templates

(def ops (partial send {:channel "ops"}))
(def community (partial send {:channel "community"}))
(def log (partial send {:channel "webserver"}))
