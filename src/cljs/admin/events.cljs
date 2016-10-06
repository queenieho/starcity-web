(ns admin.events
  (:require [admin.db :refer [default-value]]
            [admin.application.list.events]
            [admin.application.entry.events]
            [admin.notify.events]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx]]))

(reg-event-db
 :app/initialize
 (fn [_ _]
   default-value))

(reg-event-db
 :nav/home
 (fn [db _]
   (assoc db :route :home)))
