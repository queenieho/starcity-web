(ns starcity.models.apply.help
  (:require [starcity.models.account :refer [full-name]]
            [starcity.services.slack :as slack]
            [starcity.datomic :refer [conn]]
            [datomic.api :as d]))

(defn ask-question
  [account-id question k]
  (let [acct  (d/entity (d/db conn) account-id)
        email (:account/email acct)]
    (slack/send-message
     (format "An applicant is asking for help from step `%s`.\n\n*%s* asks:\n>%s\n\nHis/her email is: %s"
             k (full-name acct) question email)
     :channel "#community")))
