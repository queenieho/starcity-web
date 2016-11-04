(ns admin.account.entry.subs
  (:require [admin.account.entry.db :refer [root-db-key]]
            [admin.account.entry.security-deposit.subs]
            [admin.account.entry.model :as entry]
            [re-frame.core :refer [reg-sub]]))

;; =============================================================================
;; Internal

(reg-sub
 root-db-key
 (fn [db _]
   (get db root-db-key)))

;; =============================================================================
;; UI

(reg-sub
 :account.entry/loading?
 :<- [root-db-key]
 (fn [db _]
   (entry/is-account-loading? db)))

;; =============================================================================
;; Basic Info

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

;; =============================================================================
;; Menu

(reg-sub
 :account.entry/menu
 (fn [db _]
   (get-in db [root-db-key :menu])))

(reg-sub
 :account.entry.menu/active
 :<- [:account.entry/menu]
 (fn [menu _]
   (:active menu)))

(reg-sub
 :account.entry.menu/items
 :<- [:account.entry/menu]
 (fn [menu _]
   (:items menu)))
