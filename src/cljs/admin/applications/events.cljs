(ns admin.applications.events
  (:require [re-frame.core :refer [reg-event-fx
                                   reg-event-db]]
            [day8.re-frame.http-fx]
            [starcity.log :as l]
            [ajax.core :as ajax]))

;; Reached when the applications page is navigated to.
(reg-event-fx
 :nav/applications
 (fn [{:keys [db]} _]
   {:db       (assoc db :route :applications)
    :dispatch [:applications/fetch]}))

(def applications-endpoint
  "/api/v1/admin/applications")

;;; Fetch applications

(reg-event-fx
 :applications/fetch
 (fn [{:keys [db]} _]
   {:db         (assoc db :loading true)
    :http-xhrio {:method          :get
                 :uri             applications-endpoint
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:applications.fetch/success]
                 :on-failure      [:applications.fetch/fail]}}))

(reg-event-db
 :applications.fetch/success
 (fn [db [_ result]]
   (-> (assoc-in db [:applications :list] result)
       (assoc-in [:applications ::reset] result))))

(reg-event-db
 :applications.fetch/fail
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
 :applications.list/sort
 (fn [db [_ sort-key]]
   (let [sort     (get-in db [:applications :sort])
         next-dir (next-direction (:direction sort))
         reset    (get-in db [:applications ::reset])
         sorted   (if (= next-dir :none)
                    reset
                    (->> (get-in db [:applications :list])
                         (sort-by sort-key)))]
     (-> (assoc-in db [:applications :list] sorted)
         (assoc-in [:applications :sort] {:direction next-dir
                                          :key       sort-key})))))
