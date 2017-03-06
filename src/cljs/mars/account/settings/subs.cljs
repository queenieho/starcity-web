(ns mars.account.settings.subs
  (:require [mars.account.settings.db :as db]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::settings
 (fn [db _]
   (db/path db)))

(reg-sub
 :account.settings.change-password/changing?
 :<- [::settings]
 (fn [db _]
   (:changing-password db)))

(reg-sub
 :account.settings.change-password/form-data
 :<- [::settings]
 (fn [db _]
   (:form-data db)))
