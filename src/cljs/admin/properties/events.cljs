(ns admin.properties.events
  (:require [admin.properties.db :as db]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [path reg-event-db reg-event-fx]]
            [toolbelt.core :as tb]
            [reagent.core :as r]
            [cljs-time.format :as f]
            [cljs-time.coerce :as c]))

;; =============================================================================
;; Nav

(reg-event-fx
 :properties/navigate
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:dispatch [:properties/fetch-overview]}))

(reg-event-fx
 :property/navigate
 [(path db/path)]
 (fn [{:keys [db]} [_ property-id]]
   {:db       (db/viewing-property-id db property-id)
    :dispatch [:property/fetch property-id]}))

;; =============================================================================
;; Overview

(reg-event-fx
 :properties/fetch-overview
 [(path db/path)]
 (fn [{:keys [db]} _]
   {:db         (db/is-fetching-overview db)
    :http-xhrio {:method          :get
                 :uri             "/api/v1/admin/properties/overview"
                 :response-format (ajax/transit-response-format)
                 :on-success      [:properties.fetch-overview/success]
                 :on-failure      [:properties.fetch-overview/failure]}}))

(reg-event-fx
 :properties.fetch-overview/success
 [(path db/path)]
 (fn [{:keys [db]} [_ {result :result}]]
   {:db (db/done-fetching-overview db result)}))

(reg-event-fx
 :properties.fetch-overview/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ error]]
   (tb/error error)
   {:db            (db/error-fetching-overview db error)
    :alert/message {:type    :error
                    :content "Failed to fetch properties overview."}}))

;; =============================================================================
;; Entry

(reg-event-fx
 :property/fetch
 [(path db/path)]
 (fn [{:keys [db]} [_ property-id]]
   {:db            (db/is-fetching-property db)
    :http-xhrio    {:method          :get
                    :uri             (str "/api/v1/admin/properties/" property-id)
                    :response-format (ajax/transit-response-format)
                    :on-success      [:property.fetch/success]
                    :on-failure      [:property.fetch/failure]}}))

(reg-event-fx
 :property.fetch/success
 [(path db/path)]
 (fn [{:keys [db]} [_ {property :result}]]
   {:db (db/done-fetching-property db property)}))

(reg-event-fx
 :property.fetch/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ error]]
   {:db            (db/error-fetching-property db error)
    :alert/message {:type    :error
                    :content "Failed to fetch property!"}}))

(reg-event-fx
 :property.update/ops-fee
 [(path db/path)]
 (fn [_ [_ property-id new-fee]]
   (let [params {:property-id property-id
                 :key         :property/ops-fee
                 :value       new-fee
                 :display     (str new-fee "%")}]
     {:dispatch-throttle {:id              :property.update/ops-fee
                          :dispatch        [:property/update params]
                          :window-duration 4000
                          :trailing?       true
                          :leading?        false}})))

(def ^:private date-formatter (f/formatter "yyyy-MM-dd"))

(reg-event-fx
 :property.update/available-on
 [(path db/path)]
 (fn [_ [_ property-id new-available-on]]
   (let [params {:property-id property-id
                 :key         :property/available-on
                 :value       (c/to-date (f/parse date-formatter new-available-on))
                 :display     new-available-on}]
     {:dispatch-throttle {:id              :property.update/available-on
                          :dispatch        [:property/update params]
                          :window-duration 4000
                          :trailing?       true
                          :leading?        false}})))

(reg-event-fx
 :property.update/license
 [(path db/path)]
 (fn [_ [_ property-id new-license]]
   (let [params {:property-id property-id
                 :key         :property/licenses
                 :value       new-license
                 :display     (str "$" (:property-license/base-price new-license) " ("
                                   (:property-license/term new-license) " month)")}]
     {:dispatch-throttle {:id              (keyword "property.update" (:db/id new-license))
                          :dispatch        [:property/update params]
                          :window-duration 4000
                          :trailing?       true
                          :leading?        false}})))

(reg-event-fx
 :property/update
 [(path db/path)]
 (fn [{:keys [db]} [_ {:keys [property-id key value] :as params}]]
   {:db            (db/is-updating-property db)
    :http-xhrio    {:method          :post
                    :uri             (str "/api/v1/admin/properties/" property-id)
                    :params          {key value}
                    :format          (ajax/transit-request-format)
                    :response-format (ajax/transit-response-format)
                    :on-success      [:property.update/success params]
                    :on-failure      [:property.update/failure params]}
    :alert/message {:type     :loading
                    :duration :indefinite
                    :content  "Saving..."}}))

(reg-event-fx
 :property.update/success
 [(path db/path)]
 (fn [{:keys [db]} [_ {:keys [key value display] :as params} result]]
   {:db                 (db/done-updating-property db)
    :alert/notify       {:type    :success
                         :title   "Update success"
                         :content (r/as-element
                                   [:p "Successfully updated " [:code (str key)] " to " (or display value) "."])}
    :alert.message/hide true}))

(reg-event-fx
 :property.update/failure
 [(path db/path)]
 (fn [{:keys [db]} [_ {:keys [key value display]} error]]
   {:db                 (db/error-updating-property db error)
    :alert/notify       {:type    :error
                         :title   "Update failed"
                         :content (r/as-element
                                   [:p "Failed to update " [:code (str key)] " to " (or display value) "."])}
    :alert.message/hide true}))
