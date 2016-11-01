(ns admin.account.entry.subs
  (:require [admin.account.entry.db :refer [root-db-key]]
            [admin.account.entry.model :as entry]
            [starcity.components.tabs :as tabs]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 root-db-key
 (fn [db _]
   (get db root-db-key)))

(reg-sub
 :account.entry/loading?
 :<- [root-db-key]
 (fn [db _]
   (entry/is-account-loading? db)))

(reg-sub
 :account.entry/full-name
 :<- [root-db-key]
 (fn [db _]
   (entry/full-name db)))

(reg-sub
 :account.entry/role
 :<- [root-db-key]
 (fn [db _]
   (entry/role db)))

(reg-sub
 :account.entry/phone-number
 :<- [root-db-key]
 (fn [db _]
   (entry/phone-number db)))

(reg-sub
 :account.entry/email
 :<- [root-db-key]
 (fn [db _]
   (entry/email db)))

(tabs/install-subscriptions root-db-key)
