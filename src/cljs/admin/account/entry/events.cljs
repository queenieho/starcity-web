(ns admin.account.entry.events
  (:require [admin.account.entry.db :refer [root-db-key]]
            [admin.account.entry.security-deposit.events]
            [admin.account.entry.model :as entry]
            [admin.routes :as routes]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [day8.re-frame.http-fx]
            [admin.api :as api]
            [ajax.core :as ajax]
            [starcity.log :as l]))

(defn- fetch-events [account-id section]
  (let [events [[:account.entry/fetch account-id]]]
    (case section
      :security-deposit (conj events [:account.entry.security-deposit/fetch account-id])
      events)))

(reg-event-fx
 :nav/account
 (fn [{:keys [db]} [_ id section]]
   {:db       (-> (assoc db :route :account/entry)
                  (entry/set-active-section section))
    :dispatch-n (fetch-events id section)}))

(reg-event-fx
 :account.entry/fetch
 (fn [{:keys [db]} [_ id]]
   {:db         (if (nil? (entry/current-account db))
                  (entry/account-loading db true)
                  db)
    :http-xhrio {:method          :get
                 :uri             (api/route (str "accounts/" id))
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:account.entry.fetch/success]
                 :on-failure      [:account.entry.fetch/failure]}}))

(reg-event-db
 :account.entry.fetch/success
 (fn [db [_ result]]
   (-> (entry/account-loading db false)
       (entry/current-account result))))

(reg-event-fx
 :account.entry.fetch/failure
 (fn [{:keys [db]} [_ {:keys [response]}]]
   (let [msg (or (:error response) "Failed to fetch account.")]
     {:db       (entry/account-loading db false)
      :dispatch [:notify/error msg]})))

(reg-event-fx
 :account.entry.menu/change-tab
 (fn [{:keys [db]} [_ new-tab]]
   (let [current-id (get-in db [root-db-key :current-id])]
     {:route (routes/account-section {:id current-id :section (name new-tab)})})))
