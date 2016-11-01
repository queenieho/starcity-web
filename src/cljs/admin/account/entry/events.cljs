(ns admin.account.entry.events
  (:require [admin.account.entry.db :refer [root-db-key]]
            [admin.account.entry.model :as entry]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [day8.re-frame.http-fx]
            [admin.api :as api]
            [ajax.core :as ajax]
            [starcity.log :as l]
            [starcity.components.tabs :as tabs]))

(reg-event-fx
 :nav/account
 (fn [{:keys [db]} [_ id]]
   {:db       (assoc db :route :account/entry)
    :dispatch [:account.entry/fetch id]}))

(reg-event-fx
 :account.entry/fetch
 (fn [{:keys [db]} [_ id]]
   {:db         (entry/toggle-account-loading db)
    :http-xhrio {:method          :get
                 :uri             (api/route (str "accounts/" id))
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:account.entry.fetch/success]
                 :on-failure      [:account.entry.fetch/failure]}}))

(reg-event-db
 :account.entry.fetch/success
 (fn [db [_ result]]
   (-> (entry/toggle-account-loading db)
       (entry/current-account result))))

(reg-event-fx
 :account.entry.fetch/failure
 (fn [{:keys [db]} [_ {:keys [response]}]]
   (let [msg (or (:error response) "Failed to fetch account.")]
     {:db       (entry/toggle-account-loading db)
      :dispatch [:notify/error msg]})))

(tabs/install-events root-db-key)
