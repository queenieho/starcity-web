(ns mars.account.rent.link-account.deposits.subs
  (:require [re-frame.core :refer [reg-sub]]
            [mars.account.rent.link-account.deposits.db :as db]))

(reg-sub
 ::deposits
 (fn [db _]
   (db/path db)))

(reg-sub
 :rent.link-account.deposits/countries
 :<- [::deposits]
 (fn [db _]
   (db/countries db)))

(reg-sub
 :rent.link-account.deposits/currencies
 :<- [::deposits]
 (fn [db _]
   (db/currencies db)))

(reg-sub
 :rent.link-account.deposits.bank-info/form-data
 :<- [::deposits]
 (fn [db _]
   (db/form-data db)))

(reg-sub
 :rent.link-account.deposits.bank-info/errors
 :<- [::deposits]
 (fn [db _]
   (db/form-errors db)))

(reg-sub
 :rent.link-account.deposits.bank-info/can-submit?
 :<- [::deposits]
 (fn [db _]
   (db/can-submit? db)))

(reg-sub
 :rent.link-account.deposits.bank-info/submitting?
 :<- [::deposits]
 (fn [db _]
   (db/submitting-bank-info? db)))

(reg-sub
 :rent.link-account.deposits/amounts
 :<- [::deposits]
 (fn [db _]
   (db/deposit-amounts db)))

(reg-sub
 :rent.link-account.deposits.amounts/can-submit?
 :<- [::deposits]
 (fn [db _]
   (db/can-submit-deposits? db)))

(reg-sub
 :rent.link-account.deposits.amounts/submitting?
 :<- [::deposits]
 (fn [db _]
   (db/submitting-deposits? db)))
