(ns mars.account.rent.history.subs
  (:require [re-frame.core :refer [reg-sub]]
            [mars.account.rent.history.db :as db]))

(reg-sub
 ::history
 (fn [db _]
   (db/path db)))

(reg-sub
 :rent.history/loading?
 :<- [::history]
 (fn [db _]
   (db/loading? db)))

(reg-sub
 :rent.history/items
 :<- [::history]
 (fn [db _]
   (db/items db)))
