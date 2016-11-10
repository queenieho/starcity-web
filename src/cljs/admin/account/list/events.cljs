(ns admin.account.list.events
  (:require [admin.account.list.db :refer [root-db-key]]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [ajax.core :as ajax]
            [starcity.log :as l]))

(def endpoint
  "The API endpoint for fetching accounts."
  "/api/v1/admin/accounts")

(defn- dispatch-fetch
  [table-data & {:keys [direction sort-key limit offset view query]}]
  (let [{d :direction k :key} (:sort table-data)
        {l :limit o :offset}  (:pagination table-data)]
    [:account.list/fetch
     (or limit l)
     (or offset o)
     (or direction d)
     (or sort-key k)
     (or view (:view table-data))
     (or query (:query table-data))]))

(reg-event-fx
 :nav/accounts
 (fn [{:keys [db]} _]
   (let [data (get db root-db-key)]
     {:db       (assoc db :route :account/list)
      :dispatch (dispatch-fetch data :offset 0)})))

(reg-event-fx
 :account.list/fetch
 (fn [{:keys [db]} [_ limit offset direction sort-key view query]]
   {:db         (assoc-in db [root-db-key :loading] true)
    :http-xhrio {:method          :get
                 :uri             endpoint
                 :params          {:limit     limit
                                   :offset    offset
                                   :direction (name direction)
                                   :sort-key  (name sort-key)
                                   :view      (name view)
                                   :q         query}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:account.list.fetch/success limit offset direction sort-key view query]
                 :on-failure      [:account.list.fetch/failure]}}))

(reg-event-db
 :account.list.fetch/success
 (fn [db [_ limit offset direction sort-key view query result]]
   (let [new-data {:loading    false
                   :list       (:accounts result)
                   :total      (:total result)
                   :pagination {:limit  limit
                                :offset (+ offset (count result))}
                   :sort       {:direction direction
                                :key       sort-key}
                   :view       view
                   :query      query}]
     (update db root-db-key merge new-data))))

(reg-event-db
 :account.list.fetch/failure
 (fn [db [_ err]]
   (l/error err)
   (assoc-in db [root-db-key :loading] false)))

(defn- next-direction [curr]
  (if (= curr :asc) :desc :asc))

(reg-event-fx
 :account.list/sort
 (fn [{:keys [db]} [_ sort-key]]
   (let [data     (get db root-db-key)
         next-dir (next-direction (get-in data [:sort :direction]))]
     {:dispatch (dispatch-fetch data
                                :offset 0
                                :sort-key sort-key
                                :direction next-dir)})))

(reg-event-fx
 :account.list.query/change
 (fn [{:keys [db]} [_ new-query]]
   {:dispatch (dispatch-fetch (get db root-db-key)
                              :offset 0
                              :query new-query)}))

(reg-event-fx
 :account.list.view/change
 (fn [{:keys [db]} [_ new-view]]
   {:dispatch (dispatch-fetch (get db root-db-key)
                              :offset 0
                              :view new-view)}))

(reg-event-fx
 :account.list.pagination/next
 (fn [{:keys [db]} _]
   (let [data                   (get db root-db-key)
         {:keys [offset limit]} (:pagination data)]
     {:dispatch (dispatch-fetch (get db root-db-key)
                                :offset (+ offset limit))})))

(reg-event-fx
 :account.list.pagination/previous
 (fn [{:keys [db]} _]
   (let [data                   (get db root-db-key)
         {:keys [offset limit]} (:pagination data)]
     {:dispatch (dispatch-fetch data
                                :offset (- offset limit))})))


(reg-event-fx
 :account.list.pagination/goto-page
 (fn [{:keys [db]} [_ page-num]]
   (let [data            (get db root-db-key)
         {:keys [limit]} (:pagination data)]
     {:dispatch (dispatch-fetch data
                                :offset (* limit page-num))})))
