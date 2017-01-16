(ns mars.menu.subs
  (:require [re-frame.core :refer [reg-sub]]
            [mars.menu.db :as db]
            [starcity.log :as l]))

;; Internal sub to select subdata
(reg-sub
 ::menu
 (fn [db _]
   (db/path db)))

(reg-sub
 :menu/items
 :<- [::menu]
 (fn [db _]
   (:items db)))

(reg-sub
 :menu/active
 :<- [::menu]
 (fn [db _]
   (:active db)))

(reg-sub
 :menu/open-submenus
 :<- [::menu]
 (fn [db _]
   (:open-menus db)))
