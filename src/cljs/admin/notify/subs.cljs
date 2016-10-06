(ns admin.notify.subs
  (:require [admin.notify.db :refer [root-db-key]]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 root-db-key
 (fn [db _]
   (get db root-db-key)))

(reg-sub
 :notify/all
 :<- [root-db-key]
 (fn [data _]
   (:notifications data)))
