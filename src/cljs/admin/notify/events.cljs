(ns admin.notify.events
  (:require [admin.notify.db :refer [root-db-key]]
            [re-frame.core :refer [reg-event-db reg-event-fx]]
            [starcity.utils :refer [remove-at]]))

(reg-event-fx
 :notify/success
 (fn [{:keys [db]} [_ message]]
   {:db             (update-in db [root-db-key :notifications] conj {:message message :type :success})
    :dispatch-later [{:ms 4000 :dispatch [:notify/clear]}]}))

(reg-event-fx
 :notify/error
 (fn [{:keys [db]} [_ message]]
   {:db             (update-in db [root-db-key :notifications] conj {:message message :type :error})
    :dispatch-later [{:ms 8000 :dispatch [:notify/clear]}]}))

(reg-event-db
 :notify/delete
 (fn [db [_ idx]]
   (update-in db [root-db-key :notifications] remove-at idx)))

(reg-event-db
 :notify/clear
 (fn [db _]
   (assoc-in db [root-db-key :notifications] [])))
