(ns starcity.services.slack
  (:require [starcity.config.slack :as config]
            [starcity.environment :refer [is-development? environment]]
            [org.httpkit.client :as http]
            [cheshire.core :as json]
            [plumbing.core :refer [assoc-when]]
            [mount.core :as mount :refer [defstate]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private default-channel
  "#webserver")

;; TODO: Move to config
(def ^:private usernames
  {:staging     "staging"
   :production  "production"
   :development "debug"})

(defn- assoc-channel-when
  "Assoc the channel into opts when channel is non-nil.

  If we're in development, override the channel with the `default-channel`."
  [opts channel]
  (if channel
    (if (is-development?)
      (assoc opts :channel default-channel)
      (assoc opts :channel channel))
    opts))

;; =============================================================================
;; API
;; =============================================================================

(defn send-message
  [text & {:keys [channel cb opts] :or {cb identity, opts {}}}]
  (let [username (get usernames environment)]
    (http/post config/webhook-url
               {:headers {"Content-Type" "application/json"}
                :body    (json/generate-string (-> {:text     text
                                                    :username username}
                                                   (assoc-channel-when channel)
                                                   (merge opts)))}
               cb)))

(defn rich-message
  [title text & {:keys [channel cb opts] :or {cb identity, opts {}}}]
  (let [username (get usernames environment)
        opts     (assoc opts :fallback (or (:fallback opts) text))
        payload  (-> {:username    username
                      :attachments [(merge {:text text :title title} opts)]}
                     (assoc-channel-when channel))]
    (http/post config/webhook-url
               {:headers {"Content-Type" "application/json"}
                :body    (json/generate-string payload)}
               cb)))
