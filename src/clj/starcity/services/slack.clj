(ns starcity.services.slack
  (:require [org.httpkit.client :as http]
            [starcity.config :refer [config]]
            [cheshire.core :as json]
            [starcity.environment :refer [environment]]
            [plumbing.core :refer [assoc-when]]
            [mount.core :as mount :refer [defstate]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private usernames
  {:staging     "staging"
   :production  "production"
   :development "debug"})

(defn- webhook-request
  [webhook-url]
  (fn [text & {:keys [channel cb opts]
              :or   {cb identity, opts {}}}]
    (let [username (get usernames environment)]
      (http/post webhook-url
                 {:headers {"Content-Type" "application/json"}
                  :body    (json/generate-string (-> {:text     text
                                                      :username username}
                                                     (assoc-when :channel channel)
                                                     (merge opts)))}
                 cb))))

(defn- rich-webhook-request
  [webhook-url]
  (fn [title text & {:keys [channel cb opts]
                    :or   {cb identity, opts {}}}]
    (let [username (get usernames environment)
          opts     (assoc opts :fallback (or (:fallback opts) text))
          payload  (-> {:username    username
                        :attachments [(merge {:text text :title title} opts)]}
                       (assoc-when :channel channel))]
      (http/post webhook-url
                 {:headers {"Content-Type" "application/json"}
                  :body    (json/generate-string payload)}
                 cb))))

;; =============================================================================
;; API
;; =============================================================================

(defstate send-message
  :start (webhook-request (get-in config [:slack :webhook])))

(defstate rich-message
  :start (rich-webhook-request (get-in config [:slack :webhook])))
