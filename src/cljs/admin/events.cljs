(ns admin.events
  (:require [admin.accounts.events]
            [admin.properties.events]
            [admin.licenses.events]
            [admin.units.events]
            [admin.db :as db]
            [admin.routes :as routes]
            [re-frame.core :refer [path reg-event-db reg-event-fx]]
            [toolbelt.core :refer [str->int]]))

;; =============================================================================
;; App

(reg-event-db
 :app/initialize
 (fn [_ _]
   db/default-value))

;; =============================================================================
;; Navigation

(defn- route-dispatches [page params]
  (cond-> [[:menu/select-item page]]
    (#{:accounts} page)   (conj [:accounts/navigate])
    (#{:account} page)    (conj [:account/navigate
                                 (str->int (:account-id params))])
    (#{:properties} page) (conj [:properties/navigate])
    (#{:property} page)   (conj [:property/navigate
                                 (str->int (:property-id params))])
    (#{:unit} page)       (conj [:unit/navigate
                                 (str->int (:property-id params))
                                 (str->int (:unit-id params))])))

(reg-event-fx
 :app/route
 [(path db/nav-path)]
 (fn [{:keys [db]} [_ page params]]
   {:db         (db/route db {:page page :params params})
    :dispatch-n (route-dispatches page params)}))

;; =============================================================================
;; Menu

(defn- new-route [key]
  (let [page (db/key->page key)]
    (case page
      :accounts   (routes/path-for :accounts)
      :properties (routes/path-for :properties)
      (routes/path-for :home))))

(reg-event-fx
 :menu/item-selected
 (fn [_ [_ val]]
   {:route (new-route val)}))

(reg-event-db
 :menu/select-item
 [(path db/nav-path)]
 (fn [db [_ page]]
   (db/select-item db page)))
