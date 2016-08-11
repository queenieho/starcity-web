(ns starcity.services.mailgun
  (:require [mailgun.mail :as mail]
            [starcity.config :refer [config]]
            [mount.core :as mount :refer [defstate]]))

;; TODO: Make async?
(defn- create-mailgun-sender
  [{:keys [api-key domain sender]}]
  (let [creds {:key api-key :domain domain}]
    (fn send-email*
      ([to subject content]
       (send-email* to sender subject content))
      ([to from subject content]
       (mail/send-mail creds {:from    from
                              :to      to
                              :subject subject
                              :html    content})))))

(defstate send-email
  :start (create-mailgun-sender (:mailgun config)))
