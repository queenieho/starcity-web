(ns admin.application.list.events
  (:require [admin.application.list.db :refer [root-db-key]]
            [admin.routes :as routes]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [day8.re-frame.http-fx]
            [starcity.log :as l]
            [ajax.core :as ajax]))

(def endpoint
  "The API endpoint for applications."
  "/api/v1/admin/applications")

(defn- dispatch-fetch
  [table-data & {:keys [direction sort-key limit offset view query]}]
  (let [{d :direction k :key} (:sort table-data)
        {l :limit o :offset}  (:pagination table-data)]
    [:application.list/fetch
     (or limit l)
     (or offset o)
     (or direction d)
     (or sort-key k)
     (or view (:view table-data))
     (or query (:query table-data))]))

(defn- next-url
  [table-data & {:keys [limit offset view sort-key direction query]}]
  (let [name*                 (fn [k] (if (keyword? k) (name k) k))
        {d :direction k :key} (:sort table-data)
        {l :limit o :offset}  (:pagination table-data)
        v                     (:view table-data)
        q                     (:query table-data)]
    (routes/applications
     {:query-params {:limit     (or limit l)
                     :offset    (or offset o)
                     :direction (name* (or direction d))
                     :sort-key  (name* (or sort-key k))
                     :view      (name* (or view v))
                     :q         (or query q)}})))

;; Reached when the applications page is navigated to.
(reg-event-fx
 :nav/applications
 (fn [{:keys [db]} [_ {:keys [limit offset q view sort-key direction] :as params}]]
   (let [data (get db root-db-key)
         fx   {:db (assoc db :route :application/list)}]
     (->> (if (empty? params)
            {:route (next-url data :offset 0)}
            {:dispatch (dispatch-fetch data
                                       :offset offset
                                       :limit limit
                                       :view view
                                       :sort-key sort-key
                                       :direction direction
                                       :query q)})
          (merge fx)))))

;;; Fetch applications

(reg-event-fx
 :application.list/fetch
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
                 :on-success      [:application.list.fetch/success limit offset direction sort-key view query]
                 :on-failure      [:application.list.fetch/failure]}}))

(reg-event-db
 :application.list.fetch/success
 (fn [db [_ limit offset direction sort-key view _ result]]
   (let [new-data {:loading    false
                   :list       (:applications result)
                   :total      (:total result)
                   :pagination {:limit  limit
                                :offset (+ offset (count result))}
                   :sort       {:direction direction
                                :key       sort-key}
                   :view       view}]
     (update db root-db-key merge new-data))))

(reg-event-db
 :application.list.fetch/failure
 (fn [db [_ err]]
   (l/error err)
   (assoc-in db [root-db-key :loading] false)))

(defn- next-direction [curr]
  (if (= curr :asc) :desc :asc))

(reg-event-fx
 :application.list/sort
 (fn [{:keys [db]} [_ sort-key]]
   (let [data     (get db root-db-key)
         next-dir (next-direction (get-in data [:sort :direction]))]
     {:route (next-url data
                       :offset 0
                       :sort-key sort-key
                       :direction next-dir)})))

(reg-event-fx
 :application.list.query/change
 (fn [{:keys [db]} [_ new-query]]
   {:db                (assoc-in db [root-db-key :query] new-query)
    :dispatch-throttle {:id              :application.list.query/change
                        :window-duration 400
                        :trailing?       true
                        :leading?        false
                        :dispatch        [:application.list.query/change* new-query]}}))

(reg-event-fx
 :application.list.query/change*
 (fn [{:keys [db]} [_ new-query]]
   {:route (next-url (get db root-db-key)
                     :offset 0
                     :query new-query)}))

(reg-event-fx
 :application.list.view/change
 (fn [{:keys [db]} [_ new-view]]
   {:route (next-url (get db root-db-key)
                     :offset 0
                     :view new-view)}))

(reg-event-fx
 :application.list.pagination/next
 (fn [{:keys [db]} _]
   (let [data                   (get db root-db-key)
         {:keys [offset limit]} (:pagination data)]
     {:route (next-url (get db root-db-key)
                       :offset (+ offset limit))})))

(reg-event-fx
 :application.list.pagination/previous
 (fn [{:keys [db]} _]
   (let [data                   (get db root-db-key)
         {:keys [offset limit]} (:pagination data)]
     {:route (next-url data
                       :offset (- offset limit))})))


(reg-event-fx
 :application.list.pagination/goto-page
 (fn [{:keys [db]} [_ page-num]]
   (let [data            (get db root-db-key)
         {:keys [limit]} (:pagination data)]
     {:route (next-url data
                       :offset (* limit page-num))})))
