(ns admin.account.entry.db
  (:require [starcity.components.tabs :as tabs]))

(def root-db-key :account/entry)

(def default-value
  (merge {:accounts       {}
          :loading        {:account false}}
         (tabs/default-db :move-in
                          (tabs/tab :move-in "Move-in Checklist")
                          (tabs/tab :security-deposit "Security Deposit"))))
