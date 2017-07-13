(ns starcity.api.mars.rent.autopay
  (:require [blueprints.models.account :as account]
            [blueprints.models.news :as news]
            [compojure.core :refer [defroutes GET POST]]
            [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.models.autopay :as autopay]
            [starcity.util.request :as request]
            [starcity.util.response :as res]
            [taoensso.timbre :as timbre]))

(defn- subscribed-handler
  "Handles requests to determine whether or not the requesting user is
  subscribed to autopay or not."
  [req]
  (let [account (request/requester (d/db conn) req)]
    (res/json-ok {:subscribed (autopay/subscribed? (d/db conn) account)})))

(def ^:private already-subscribed
  (res/json-unprocessable {:error "You are already subscribed to autopay -- cannot subscribe again."}))

(defn- subscribe-handler
  "Subscribes requesting user to autopay."
  [{:keys [params] :as req}]
  (let [account (request/requester (d/db conn) req)]
    (if (autopay/subscribed? (d/db conn) account)
      already-subscribed
      (try
        (autopay/subscribe! conn account)
        ;; Dismiss the news item that prompted `account` to set up autopay, as it's now setup.
        (when-some [news (news/by-action (d/db conn) account news/autopay-action)]
          @(d/transact conn [(news/dismiss news)]))
        (res/json-ok {:status (autopay/setup-status (d/db conn) account)})
        (catch Exception e
          (timbre/error e ::subscribe {:account (account/email account)})
          (throw e))))))

(defroutes routes
  (GET "/subscribed" [] subscribed-handler)

  (POST "/subscribe" [] subscribe-handler))
