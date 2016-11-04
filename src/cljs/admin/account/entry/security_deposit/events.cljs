(ns admin.account.entry.security-deposit.events
  (:require [admin.account.entry.security-deposit.db :as model :refer [root-db-key]]
            [admin.account.entry.model :refer [current-account-id]]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [admin.api :as api]
            [ajax.core :as ajax]
            [starcity.log :as l]))

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

(reg-event-fx
 :account.entry.security-deposit.fetch/failure
 (fn [{:keys [db]} [_ {:keys [response]}]]
   (let [msg (or (:error response) "Failed to fetch security deposit data.")]
     {:db       (model/set-loading db false)
      :dispatch [:notify/error msg]})))

(reg-event-db
 :account.entry.security-deposit.check/show-modal
 (fn [db [_ data]]
   (-> (model/show-check-modal db)
       (model/set-check-modal-data data))))

(reg-event-db
 :account.entry.security-deposit.check/hide-modal
 (fn [db _]
   (model/hide-check-modal db)))

(reg-event-db
 :account.entry.security-deposit.check/update
 (fn [db [_ k v]]
   (model/update-check-data db k v)))

(reg-event-fx
 :account.entry.security-deposit/save-check
 (fn [{:keys [db]} [_ check-data]]
   (let [account-id (current-account-id db)]
     {:http-xhrio {:method          :post
                   :uri             (api/route (str "accounts/" account-id "/security-deposit/check"))
                   :params          (if (empty? (:bank check-data)) (dissoc check-data :bank) check-data)
                   :format          (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:account.entry.security-deposit.save-check/success]
                   :on-failure      [:account.entry.security-deposit.save-check/failure]}})))

(reg-event-fx
 :account.entry.security-deposit.save-check/success
 (fn [{:keys [db]} _]
   {:dispatch-n [[:account.entry.security-deposit/fetch (current-account-id db)]
                 [:account.entry.security-deposit.check/hide-modal]]}))

(reg-event-fx
 :account.entry.security-deposit.save-check/failure
 (fn [{:keys [db]} [_ {:keys [response]}]]
   (let [msg (or (:error response) "Error encountered while attempting to create check.")]
     (l/error response)
     {:dispatch-n [[:notify/error msg]
                   [:account.entry.security-deposit.check/hide-modal]]})))
