(ns admin.units.events
  (:require [admin.units.db :as db]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [path reg-event-db reg-event-fx]]))

;; =============================================================================
;; Nav

(reg-event-fx
 :unit/navigate
 [(path db/path)]
 (fn [{:keys [db]} [_ property-id unit-id]]
   {:db       (db/viewing-unit-id db unit-id)
    :dispatch-n [[:property/fetch property-id]
                 [:unit/fetch property-id unit-id]]}))

(reg-event-fx
 :unit/fetch
 [(path db/path)]
 (fn [{:keys [db]} [_ property-id unit-id]]
   {:db            (db/is-fetching-unit db)
    :http-xhrio    {:method          :get
                    :uri             (str "/api/v1/admin/properties/"
                                          property-id "/units/" unit-id)
                    :response-format (ajax/transit-response-format)
                    :on-success      [:unit.fetch/success]
                    :on-failure      [:unit.fetch/failure]}}))

(reg-event-db
 :unit.fetch/success
 [(path db/path)]
 (fn [db [_ {unit :result}]]
   (db/done-fetching-unit db unit)))

(reg-event-fx
 :unit.fetch/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ error]]
   {:db            (db/error-fetching-unit db error)
    :alert/message {:type    :error
                    :content "Failed to fetch unit!"}}))

(defn- license-price-uri
  [{:keys [property-id unit-id license-price-id]}]
  (let [suffix (if license-price-id
                 (str "/license-prices/" license-price-id)
                 "/license-prices")]
    (str "/api/v1/admin/properties/" property-id "/units/" unit-id suffix)))

(reg-event-fx
 :unit.license-price/update
 [(path db/path)]
 (fn [{:keys [db]} [_ {:keys [price term property-id unit-id] :as event}]]
   {:http-xhrio    {:method          :post
                    :uri             (license-price-uri event)
                    :params          {:price price
                                      :term  term}
                    :format          (ajax/transit-request-format)
                    :response-format (ajax/transit-response-format)
                    :on-success      [:unit.license-price.update/success property-id unit-id]
                    :on-failure      [:unit.license-price.update/failure]}
    :alert/message {:type     :loading
                    :content  "Saving..."
                    :duration :indefinite}}))

(reg-event-fx
 :unit.license-price.update/success
 [(path db/path)]
 (fn [{:keys [db]} [_ property-id unit-id _]]
   {:alert.message/hide true
    :dispatch           [:unit/fetch property-id unit-id]}))

(reg-event-fx
 :unit.license-price.update/failure
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:alert.message/hide true
    :alert/notify       {:type    :error
                         :title   "Failed to save license price!"
                         :content "Something went wrong server-side."}}))

(reg-event-fx
 :unit.license-price/remove
 [(path db/path)]
 (fn [_ [_ property-id unit-id price-id]]
   {:http-xhrio    {:method          :delete
                    :uri             (license-price-uri {:property-id      property-id
                                                         :license-price-id price-id
                                                         :unit-id          unit-id})
                    :format          (ajax/transit-request-format)
                    :response-format (ajax/transit-response-format)
                    :on-success      [:unit.license-price.remove/success property-id unit-id]
                    :on-failure      [:unit.license-price.remove/failure]}
    :alert/message {:type     :loading
                    :content  "Saving..."
                    :duration :indefinite}}))


(reg-event-fx
 :unit.license-price.remove/success
 [(path db/path)]
 (fn [{:keys [db]} [_ property-id unit-id _]]
   {:alert.message/hide true
    :dispatch           [:unit/fetch property-id unit-id]}))

(reg-event-fx
 :unit.license-price.remove/failure
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:alert.message/hide true
    :alert/notify       {:type    :error
                         :title   "Failed to remove license price!"
                         :content "Something went wrong server-side."}}))
