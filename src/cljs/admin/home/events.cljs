(ns admin.home.events
  (:require [admin.home.db :as db]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [plumbing.core :as plumbing]
            [ajax.core :as ajax]
            [toolbelt.core :as tb]))

(reg-event-fx
 :home/initialize
 (fn [_ _]
   {:dispatch [:home.metrics/fetch]}))

(reg-event-db
 :home.metrics.controls/update
 [(path db/path)]
 (fn [db [_ k v]]
   (assoc-in db [:metrics :controls k] v)))

(reg-event-fx
 :home.metrics/update!
 [(path db/path)]
 (fn [{:keys [db]} [_ {:keys [from to]}]]
   {:dispatch [:home.metrics/fetch from to]}))

(reg-event-fx
 :home.metrics/fetch
 [(path db/path)]
 (fn [{:keys [db]} [_ from to]]
   {:db         (assoc-in db [:metrics :loading] true)
    :http-xhrio {:method          :get
                 :uri             "/api/v1/admin/metrics"
                 :params          (plumbing/assoc-when {} :pstart from :pend to)
                 :response-format (ajax/transit-response-format)
                 :on-success      [:home.metrics.fetch/success]
                 :on-failure      [:home.metrics.fetch/failure]}}))

(reg-event-db
 :home.metrics.fetch/success
 [(path db/path)]
 (fn [db [_ {result :result}]]
   (-> (update db :metrics merge result)
       (assoc-in [:metrics :loading] false))))

(reg-event-fx
 :home.metrics.fetch/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ {res :response :as error}]]
   (tb/error error)
   {:db            (assoc-in db [:metrics :loading] false)
    :alert/message {:type :error :content "Failed to fetch metrics."}}))
