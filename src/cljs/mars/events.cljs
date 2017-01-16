(ns mars.events
  (:require [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [mars.db :as db]
            [mars.menu.events]
            [mars.activity.events]
            [mars.account.events]))

(reg-event-fx
 :app/initialize
 (fn [_ _]
   {:db db/default-value}))

(reg-event-fx
 :app/load-scripts
 (fn [{:keys [db]} [_ & scripts]]
   {:load-scripts scripts}))

(reg-event-fx
 :nav/activity
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db       (assoc db :route :activity)
    :dispatch-n [[:menu/initialize "activity"]
                 [:activity/bootstrap]]}))

(reg-event-fx
 :nav/account
 [(path db/path)]
 (fn [{:keys [db]} [_ subsection]]
   {:db         (assoc db :route :account)
    :dispatch-n [[:menu/initialize (str "account." subsection)]
                 [:account/initialize subsection]]}))
