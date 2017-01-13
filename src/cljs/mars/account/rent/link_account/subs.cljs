(ns mars.account.rent.link-account.subs
  (:require [re-frame.core :refer [reg-sub]]
            [mars.account.rent.link-account.deposits.subs]
            [mars.account.rent.link-account.authorize.subs]
            [mars.account.rent.link-account.db :as db]))

(reg-sub
 ::setup
 (fn [db _]
   (db/path db)))

(reg-sub
 :rent.link-account/status
 :<- [::setup]
 (fn [db _]
   (db/status db)))

(reg-sub
 :rent.link-account/loading?
 :<- [::setup]
 (fn [db _]
   (db/loading db)))

(reg-sub
 :rent.link-account.plaid/loading?
 :<- [::setup]
 (fn [db _]
   (db/plaid-loading db)))
