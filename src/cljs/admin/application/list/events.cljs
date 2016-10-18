(ns admin.application.list.events
  (:require [admin.application.list.db :refer [root-db-key]]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [day8.re-frame.http-fx]
            [starcity.log :as l]
            [ajax.core :as ajax]))

;; Reached when the applications page is navigated to.
(reg-event-fx
 :nav/applications
 (fn [{:keys [db]} _]
   {:db       (assoc db :route :application/list)
    :dispatch [:application.list/fetch]}))

(def applications-endpoint
  "/api/v1/admin/applications")

;;; Fetch applications

(reg-event-fx
 :application.list/fetch
 (fn [{:keys [db]} _]
   {:db         (assoc-in db [root-db-key :loading] true)
    :http-xhrio {:method          :get
                 :uri             applications-endpoint
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:application.list.fetch/success]
                 :on-failure      [:application.list.fetch/fail]}}))

(reg-event-db
 :application.list.fetch/success
 (fn [db [_ result]]
   (-> (assoc-in db [root-db-key :list] result)
       (assoc-in [root-db-key ::reset] result))))

(reg-event-db
 :application.list.fetch/fail
 (fn [db [_ err]]
   (l/error err)
   db))

(defn- next-direction
  [curr-direction]
  (case curr-direction
    :asc :desc
    :desc :none
    :asc))

(def ^:private comp-fns
  {:asc  <
   :desc >})

(reg-event-db
 :application.list/sort
 (fn [db [_ sort-key]]
   (let [sort     (get-in db [root-db-key :sort])
         next-dir (next-direction (:direction sort))
         reset    (get-in db [root-db-key ::reset])
         sorted   (if (= next-dir :none)
                    reset
                    (->> (get-in db [root-db-key :list])
                         (sort-by sort-key (get comp-fns next-dir))))]
     (-> (assoc-in db [root-db-key :list] sorted)
         (assoc-in [root-db-key :sort] {:direction next-dir
                                          :key       sort-key})))))
