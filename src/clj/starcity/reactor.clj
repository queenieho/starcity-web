(ns starcity.reactor
  (:require [mount.core :refer [defstate]]
            [mailer.core :as mailer]
            [starcity.config :as config :refer [config]]
            [starcity.datomic :refer [conn listener]]
            [reactor.reactor :as reactor]
            [reactor.deps :as deps]
            [reactor.services.community-safety :as cs]
            [reactor.services.slack :as slack]
            [reactor.services.weebly :as weebly]
            [ribbon.core :as ribbon]
            [clojure.core.async :as a]))


(defstate community-safety
  :start (if (config/is-production? config)
           (cs/community-safety (config/community-safety-api-key config))
           (reify cs/ICommunitySafety
             (background-check [this user-id first-name last-name email dob]
               (cs/background-check this user-id first-name last-name email dob {}))
             (background-check [this user-id first-name last-name email dob opts]
               (let [c (a/chan 1)]
                 (a/put! c {:body {} :headers {:location "https://test-community-safety.joinstarcity.com"}})
                 c)))))


(defstate mailer
  :start (if (config/is-production? config)
           (mailer/mailgun (config/mailgun-api-key config)
                           (config/mailgun-domain config))
           (mailer/mailgun (config/mailgun-api-key config)
                           (config/mailgun-domain config)
                           {:sender  (config/mailgun-sender config)
                            :send-to "josh@joinstarcity.com"})))


(defstate slack
  :start (if (config/is-production? config)
           (slack/slack (config/slack-webhook-url config)
                        (config/slack-username config))
           (slack/slack (config/slack-webhook-url config)
                        (config/slack-username config)
                        "#debug")))


(defstate weebly
  :start (if (config/is-production? config)
           (weebly/weebly (config/weebly-site-id config)
                          (config/weebly-form-id config))
           (reify weebly/WeeblyPromote
             (subscribe! [this email]
               (let [c (a/chan 1)]
                 (a/put! c {:body {:email email}})
                 c)))))


(defstate stripe
  :start (ribbon/stripe-connection (config/stripe-private-key config)))


(defstate reactor
  :start (let [deps (deps/deps community-safety
                               mailer
                               slack
                               weebly
                               stripe
                               (config/hostname config))]
           (reactor/start! conn listener deps))
  :stop (reactor/stop! listener reactor))
