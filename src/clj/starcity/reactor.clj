(ns starcity.reactor
  (:require [clojure.core.async :as a]
            [mount.core :refer [defstate]]
            [starcity.config :as config :refer [config]]
            [starcity.datomic :refer [conn]]
            [reactor.reactor :as reactor]))


(defn- reactor-config []
  (if (config/is-production? config)
    {:mailer          {:api-key (config/mailgun-api-key config)
                       :domain  (config/mailgun-domain config)
                       :sender  (config/mailgun-sender config)}
     :slack           {:webhook-url (config/slack-webhook-url config)
                       :username    (config/slack-username config)}
     :stripe          {:secret-key (config/stripe-private-key config)}
     :public-hostname (config/hostname config)}
    {:mailer          {:api-key (config/mailgun-api-key config)
                       :domain  (config/mailgun-domain config)
                       :sender  (config/mailgun-sender config)
                       :send-to "josh@joinstarcity.com"}
     :slack           {:webhook-url (config/slack-webhook-url config)
                       :username    (config/slack-username config)
                       :channel     "#debug"}
     :stripe          {:secret-key (config/stripe-private-key config)}
     :public-hostname (config/hostname config)}))


(defstate reactor
  :start (let [chan (a/chan (a/sliding-buffer 2048))]
           (reactor/start! conn chan (reactor-config)))
  :stop (reactor/stop! reactor))
