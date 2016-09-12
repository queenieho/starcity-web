(ns admin.application.events
  (:require [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [day8.re-frame.http-fx]
            [starcity.log :as l]
            [admin.api :as api]
            [ajax.core :as ajax]))

(reg-event-fx
 :nav/application
 (fn [{:keys [db]} [_ id]]
   {:db (-> (assoc db :route :application)
            (assoc-in [:application :current-id] id))
    :dispatch [:application/fetch id]}))

(reg-event-fx
 :application/fetch
 (fn [{:keys [db]} [_ id]]
   {:db         (assoc db :loading true)
    :http-xhrio {:method :get
                 :uri (api/route (str "applications/" id))
                 :response-format (ajax/json-response-format {:keywords? true?})
                 :on-success [:application.fetch/success]
                 :on-failure [:application.fetch/fail]}}))

(reg-event-db
 :application.fetch/success
 (fn [db [_ result]]
   (assoc-in db [:application :applications (:id result)] result)))

(reg-event-db
 :application.fetch/fail
 (fn [db [_ err]]
   (l/error err)
   db))

;; Handle a change in current tab
(reg-event-db
 :application/view-tab
 (fn [db [_ new-tab]]
   (assoc-in db [:application :active-tab] new-tab)))
