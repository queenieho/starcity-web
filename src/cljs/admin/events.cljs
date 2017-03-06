(ns admin.events
  (:require [admin.accounts.events]
            [admin.home.events]
            [admin.licenses.events]
            [admin.notes.events]
            [admin.properties.events]
            [admin.units.events]
            [admin.db :as db]
            [admin.routes :as routes]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [path reg-event-db reg-event-fx]]
            [toolbelt.core :refer [str->int]]
            [clojure.string :as string]
            [ajax.core :as ajax]
            [toolbelt.core :as tb]))

;; =============================================================================
;; App

(reg-event-fx
 :app/initialize
 (fn [_ _]
   {:db         db/default-value
    :dispatch   [:home/initialize]
    :http-xhrio {:method          :get
                 :uri             "/api/v1/admin"
                 :response-format (ajax/transit-response-format)
                 :on-success      [:app.initialize/success]
                 :on-failure      [:app.initialize/failure]}}))

(reg-event-db
 :app.initialize/success
 (fn [db [_ {result :result}]]
   (assoc db :auth (:auth result))))

(reg-event-fx
 :app.initialize/failure
 (fn [_ [_ error]]
   (tb/error error)
   {:alert/notify
    {:type     :error
     :duration 6.0
     :title    "Initialization error"
     :content  "Could not contact server. Please check your internet connection."}}))

;; =============================================================================
;; Navigation

(defn- route-dispatches [{:keys [root page params]}]
  (cond-> [[:menu/select-item root]]
    (#{:accounts} root)   (conj [:accounts/navigate])
    (#{:account} root)    (conj [:account/navigate page (str->int (:account-id params))])
    (#{:properties} root) (conj [:properties/navigate])
    (#{:property} root)   (conj [:property/navigate
                                 (str->int (:property-id params))])
    (#{:unit} root)       (conj [:unit/navigate
                                 (str->int (:property-id params))
                                 (str->int (:unit-id params))])))
(defn- extract-root [page]
  (if-let [n (and page (namespace page))]
    (-> (string/split n #"\.") (first) (keyword))
    page))

(reg-event-fx
 :app/route
 [(path db/nav-path)]
 (fn [{:keys [db]} [_ page params]]
   (let [route      {:root (extract-root page) :page page :params params}
         dispatches (route-dispatches route)]
     {:db         (db/route db route)
      :dispatch-n dispatches})))

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
