(ns admin.accounts.check-form.events
  (:require [admin.accounts.check-form.db :as db]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [cljs.spec :as s]
            [toolbelt.core :as tb]
            [ajax.core :as ajax]))

;; Saves check type and params so we know where POST to later
(reg-event-db
 :check-form/show
 [(path db/path)]
 (fn [db [_ params]]
   (db/show db params)))

(reg-event-db
 :check-form/hide
 [(path db/path)]
 (fn [db _]
   (db/hide db)))

(reg-event-db
 :check-form/update
 [(path db/path)]
 (fn [db [_ k v]]
   (db/update-field db k v)))

(defmulti submit-check (fn [db fx] (-> db :params db/check-type)))

(defmethod submit-check :default [_ fx]
  (println "Unhandled check submission@")
  fx)

(defn- check-req [uri params]
  {:http-xhrio {:method          :post
                :uri             uri
                :params          params
                :format          (ajax/transit-request-format)
                :response-format (ajax/transit-response-format)
                :on-success      [:check-form.submit/success [:account.viewing/refresh]]
                :on-failure      [:check-form.submit/failure]}})

(defmethod submit-check :deposit
  [{:keys [params form] :as db} fx]
  (merge
   (check-req (str "/api/v1/admin/checks") (assoc form :deposit-id (:deposit-id params)))
   fx))

(defmethod submit-check :rent-payment
  [{:keys [params form] :as db} fx]
  (merge
   (check-req (str "/api/v1/admin/checks") (assoc form :payment-id (:payment-id params)))
   fx))

(defmethod submit-check :update
  [{:keys [params form edited] :as db} fx]
  (merge
   (check-req (str "/api/v1/admin/checks/" (:check-id params)) (select-keys form edited))
   fx))

(reg-event-fx
 :check-form/submit
 [(path db/path)]
 (fn [{:keys [db]} _]
   (let [params     (:params db)
         check-type (db/check-type params)]
     (submit-check db {:db (assoc db :submitting true)}))))

(reg-event-fx
 :check-form.submit/success
 [(path db/path)]
 (fn [{:keys [db]} [_ dispatch]]
   {:db         (-> (assoc db :submitting false)
                    ;; clear inputs for next time
                    (dissoc :form :params :edited))
    :dispatch-n [dispatch
                 [:check-form/hide]]}))

(reg-event-fx
 :check-form.submit/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ {res :response :as error}]]
   (tb/error error)
   {:db           (assoc db :submitting false)
    :alert/notify {:type    :error
                   :title   "Cannot save check!"
                   :content (or (:message res) "An unknown error occurred.")}
    :dispatch [:check-form/hide]}))
