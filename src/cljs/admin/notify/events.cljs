(ns admin.notify.events
  (:require [admin.notify.db :refer [root-db-key]]
            [re-frame.core :refer [reg-event-db]]
            [starcity.utils :refer [remove-at]]))

(reg-event-db
 :notify/success
 (fn [db [_ message]]
   (update-in db [root-db-key :notifications] conj {:message message :type :success})))

(reg-event-db
 :notify/error
 (fn [db [_ message]]
   (update-in db [root-db-key :notifications] conj {:message message :type :error})))

(reg-event-db
 :notify/delete
 (fn [db [_ idx]]
   (update-in db [root-db-key :notifications] remove-at idx)))
