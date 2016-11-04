(ns admin.account.entry.security-deposit.events
  (:require [admin.account.entry.security-deposit.db :as model :refer [root-db-key]]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [admin.api :as api]
            [ajax.core :as ajax]))

(reg-event-fx
 :account.entry.security-deposit/fetch
 (fn [{:keys [db]} [_ account-id]]
   {:db         (model/set-loading db)
    :http-xhrio {:method          :get
                 :uri             (api/route (str "accounts/" account-id "/security-deposit"))
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:account.entry.security-deposit.fetch/success]
                 :on-failure      [:account.entry.security-deposit.fetch/failure]}}))

(reg-event-db
 :account.entry.security-deposit.fetch/success
 (fn [db [_ deposit]]
   (-> (model/set-security-deposit db deposit)
       (model/set-loading))))
