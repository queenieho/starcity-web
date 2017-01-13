(ns mars.subs
  (:require [re-frame.core :refer [reg-sub]]
            [mars.db :as db]
            [mars.menu.subs]
            [mars.activity.subs]
            [mars.account.subs]
            [starcity.log :as l]))

(reg-sub
 ::root
 (fn [db _]
   (db/path db)))

(reg-sub
 :app/current-route
 :<- [::root]
 (fn [db _]
   (:route db)))

(reg-sub
 :app/nav-toggled?
 (fn [db _]
   (:nav-toggled db)))
