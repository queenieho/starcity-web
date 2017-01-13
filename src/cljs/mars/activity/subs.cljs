(ns mars.activity.subs
  (:require [re-frame.core :refer [reg-sub]]
            [mars.activity.db :as db]))

(reg-sub
 ::activity
 (fn [db _]
   (db/path db)))

(reg-sub
 :activity.feed/items
 :<- [::activity]
 (fn [db _]
   (get-in db [:feed :items])))

(reg-sub
 :activity.feed/loading?
 :<- [::activity]
 (fn [db _]
   (db/feed-loading? db)))
