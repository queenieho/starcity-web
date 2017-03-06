(ns admin.home.subs
  (:require [admin.home.db :as db]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::home
 (fn [db _]
   (db/path db)))

(reg-sub
 :home/metrics
 :<- [::home]
 (fn [db _]
   (select-keys (:metrics db) [:accounts/created :applications/created])))

(reg-sub
 :home.metrics/loading?
 :<- [::home]
 (fn [db _]
   (get-in db [:metrics :loading])))

(reg-sub
 :home.metrics/controls
 :<- [::home]
 (fn [db _]
   (db/metric-controls db)))
