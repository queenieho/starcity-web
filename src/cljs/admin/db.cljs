(ns admin.db
  (:require [admin.application.list.db :as applications]
            [admin.application.entry.db :as application]
            [admin.account.list.db :as accounts]
            [admin.account.entry.db :as account]
            [admin.account.entry.security-deposit.db :as sd]
            [admin.notify.db :as notify]))

(def default-value
  {applications/root-db-key applications/default-value
   application/root-db-key  application/default-value
   accounts/root-db-key     accounts/default-value
   account/root-db-key      account/default-value
   notify/root-db-key       notify/default-value
   sd/root-db-key           sd/default-value
   :route                   :home})
