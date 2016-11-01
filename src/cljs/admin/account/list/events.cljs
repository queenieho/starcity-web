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

(reg-event-fx
 :nav/accounts
 (fn [{:keys [db]} _]
   (let [data                    (get db root-db-key)
         limit                   (get-in data [:pagination :limit])
         {:keys [direction key]} (:sort data)]
     {:db       (assoc db :route :account/list)
      :dispatch [:account.list/fetch limit 0 direction key (:view data)]})))

(reg-event-fx
 :account.list/fetch
 (fn [{:keys [db]} [_ limit offset direction sort-key view]]
   {:db         (assoc-in db [root-db-key :loading] true)
    :http-xhrio {:method          :get
                 :uri             endpoint
                 :params          {:limit     limit
                                   :offset    offset
                                   :direction (name direction)
                                   :sort-key  (name sort-key)
                                   :view      (name view)}
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:account.list.fetch/success limit offset direction sort-key view]
                 :on-failure      [:account.list.fetch/failure]}
    ;; :dispatch [:account.list.fetch/success limit offset direction sort-key view]
    }))

(reg-event-db
 :account.list.fetch/success
 (fn [db [_ limit offset direction sort-key view result]]
   (let [new-data {:loading    false
                   :list       (:accounts result)
                   :total      (:total result)
                   :pagination {:limit  limit
                                :offset (+ offset (count result))}
                   :sort       {:direction direction
                                :key       sort-key}
                   :view       view}]
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
   (let [data      (get db root-db-key)
         direction (get-in data [:sort :direction])
         limit     (get-in data [:pagination :limit])
         next-dir  (next-direction direction)]
     {:dispatch [:account.list/fetch limit 0 next-dir sort-key (:view data)]})))

;; figure out how to minimize the repetition here
(reg-event-fx
 :account.list.view/change
 (fn [{:keys [db]} [_ new-view]]
   (let [{:keys [key direction]} (get-in db [root-db-key :sort])
         limit                   (get-in db [root-db-key :pagination :limit])]
     {:dispatch [:account.list/fetch limit 0 direction key new-view]})))

(reg-event-fx
 :account.list.pagination/next
 (fn [{:keys [db]} _]
   (let [{:keys [offset limit]}  (get-in db [root-db-key :pagination])
         {:keys [direction key]} (get-in db [root-db-key :sort])
         view                    (get-in db [root-db-key :view])]
     {:dispatch [:account.list/fetch limit (+ offset limit) direction key view]})))

(reg-event-fx
 :account.list.pagination/previous
 (fn [{:keys [db]} _]
   (let [{:keys [offset limit]}  (get-in db [root-db-key :pagination])
         {:keys [direction key]} (get-in db [root-db-key :sort])
         view                    (get-in db [root-db-key :view])]
     {:dispatch [:account.list/fetch limit (- offset limit) direction key view]})))


(reg-event-fx
 :account.list.pagination/goto-page
 (fn [{:keys [db]} [_ page-num]]
   (let [{:keys [offset limit]}  (get-in db [root-db-key :pagination])
         {:keys [direction key]} (get-in db [root-db-key :sort])
         view                    (get-in db [root-db-key :view])]
     {:dispatch [:account.list/fetch limit (* limit page-num) direction key view]})))
