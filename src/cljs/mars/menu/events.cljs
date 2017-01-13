(ns mars.menu.events
  (:require [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [mars.menu.db :as db]))

(reg-event-db
 :menu/initialize
 [(path db/path)]
 (fn [db [_ active-item]]
   (db/init db active-item)))

(reg-event-fx
 :menu/select
 [(path db/path)]
 (fn [{:keys [db]} [_ key]]
   {:db    (db/set-active db key)
    :route (db/key->route key)}))

(reg-event-db
 :menu.submenu/change-open
 [(path db/path)]
 (fn [db [_ new-open]]
   (db/update-open-submenus db new-open)))
