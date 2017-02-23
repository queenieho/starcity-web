(ns admin.licenses.events
  (:require [admin.licenses.db :as db]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-fx
                                   reg-event-db
                                   path]]
            [ajax.core :as ajax]
            [toolbelt.core :as tb]))

(reg-event-fx
 :licenses/fetch
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             "/api/v1/admin/licenses"
                 :response-format (ajax/transit-response-format)
                 :on-success      [::fetch-success]
                 :on-failure      [::fetch-failure]}}))

(reg-event-db
 ::fetch-success
 [(path db/path)]
 (fn [db [_ {licenses :result}]]
   (assoc db :licenses licenses)))

(reg-event-fx
 ::fetch-failure
 [(path db/path)]
 (fn [{:keys [db]} [_ error]]
   (tb/error error)
   {:alert/message {:type    :error
                    :content "Failed to fetch licenses!"}}))
