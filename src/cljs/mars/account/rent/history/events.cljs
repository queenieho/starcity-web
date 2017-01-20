(ns mars.account.rent.history.events
  (:require [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [mars.account.rent.history.db :as db]
            [day8.re-frame.http-fx]
            [mars.api :as api]
            [ajax.core :as ajax]
            [starcity.log :as l]))

(reg-event-fx
 :rent.history/bootstrap
 [(path db/path)]
 (fn [_ _]
   {:dispatch [:rent.history/fetch]}))

(reg-event-fx
 :rent.history/fetch
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db         (db/toggle-loading db)
    :http-xhrio {:method          :get
                 :uri             (api/route "/rent/payments")
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:rent.history.fetch/success]
                 :on-failure      [:rent.history.fetch/failure]}}))

(reg-event-db
 :rent.history.fetch/failure
 [(path db/path)]
 (fn [db [_ error]]
   (l/error error)
   (db/toggle-loading db)))

(reg-event-db
 :rent.history.fetch/success
 [(path db/path)]
 (fn [db [_ result]]
   (l/log result)
   (db/fetch-success db result)))

(reg-event-db
 :rent.history/set-pending-ach
 [(path db/path)]
 (fn [db [_ payment-id]]
   (db/set-pending-ach db payment-id)))
