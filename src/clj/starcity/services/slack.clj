(ns starcity.services.slack
  (:refer-clojure :exclude [send])
  (:require [cheshire.core :as json]
            [clojure.core.async :refer [chan put!]]
            [clojure.string :as str]
            [org.httpkit.client :as http]
            [plumbing.core :refer [assoc-when]]
            [starcity.config.slack :as config]
            [starcity.environment :refer [is-production?]]
            [taoensso.timbre :as timbre]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private debug-channel
  "#debug")

(defn- assoc-channel-when
  "Assoc the channel into opts when channel is non-nil.

  If we're in development, override the channel with the `debug-channel`."
  [opts channel]
  (if channel
    (if (is-production?)
      (assoc opts :channel channel)
      (assoc opts :channel debug-channel))
    opts))

(defn- assoc-channel
  "Assoc the `channel` into `params`.

  If we're in development, override the channel with the `debug-channel`."
  [opts channel]
  (if (is-production?)
    (assoc opts :channel channel)
    (assoc opts :channel debug-channel)))

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
  (if (or (str/starts-with? s "#") (str/starts-with? s "@"))
    s
    (str "#" s)))

(defn send
  [{:keys [channel username] :or {channel debug-channel}} msg & {:keys [uuid]}]
  (let [out-c      (chan 1)
        msg-params (-> {:username (or username config/username)}
                       (assoc-channel (->channel channel)))]
    (http/post config/webhook-url
               {:keepalive 30000
                :headers   {"Content-Type" "application/json"}
                :body      (json/generate-string (merge msg msg-params))}
               (fn [res]
                 (if-let [error (:error res)]
                   (do
                     (timbre/error error ::send (assoc-when
                                                 {:response (dissoc res :error)}
                                                 :uuid uuid))
                     (put! out-c error))
                   (do
                     (timbre/trace ::send (assoc-when
                                           {:response res}
                                           :uuid uuid))
                     (put! out-c true)))))
    out-c))

;;; Templates

(def ops (partial send {:channel "ops"}))
(def crm (partial send {:channel "crm"}))
(def community (partial send {:channel "community"}))
(def log (partial send {:channel "webserver"}))
