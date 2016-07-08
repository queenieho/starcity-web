(ns starcity.services.slack
  (:require [org.httpkit.client :as http]
            [starcity.config :refer [config]]
            [cheshire.core :as json]
            [starcity.environment :refer [environment]]
            [mount.core :as mount :refer [defstate]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(def ^:private usernames
  {:staging    "starcity-staging"
   :production "starcity-production"})

(defn- webhook-request*
  [text username cb]
  (http/post (get-in config [:slack :webhook])
             {:headers {"Content-Type" "application/json; charset=utf-8"}
              :body    (json/generate-string {:text     text
                                              :username username})}
             cb))

;; =============================================================================
;; API
;; =============================================================================

(defn webhook-request
  ([text]
   (webhook-request text identity))
  ([text cb]
   (let [username (get usernames environment)]
     (when-not (= environment :development)
       (webhook-request text username cb)))))
