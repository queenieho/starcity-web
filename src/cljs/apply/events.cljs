(ns apply.events
  (:require [apply.prompts.events]
            [apply.logistics.events]
            [apply.personal.events]
            [apply.community.events]
            [apply.prompts.models :as prompts]
            [apply.db :refer [default-value]]
            [apply.routes :refer [navigate!]]
            [apply.api :as api]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   reg-fx]]
            [ajax.core :as ajax]
            [starcity.log :as l]
            [apply.notifications :as n]))

;; =============================================================================
;; App Events
;; =============================================================================

;; 1. Initialialize the app by loading the default values for the DB
;; 2. Pull server-side data
(reg-event-fx
 :app/initialize
 (fn [_ _]
   {:db       default-value
    :dispatch [:app.initialize/fetch]}))

;; Pull current progress
(reg-event-fx
 :app.initialize/fetch
 (fn [{:keys [db]} _]
   {:db         (assoc db :app/initializing true)
    :http-xhrio {:method          :get
                 :uri             (api/route)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:app.initialize.fetch/success]
                 :on-failure      [:app.initialize.fetch/fail]}}))


(reg-event-fx
 :app.initialize.fetch/success
 (fn [{:keys [db]} [_ {:keys [properties licenses] :as result}]]
   {:db       (merge db {:app/properties   (sort-by :property/available-on properties)
                         :app/licenses     (sort-by :license/term > licenses)
                         :app/initializing false})
    :dispatch [:app/parse result]}))

(reg-event-fx
 :app/parse
 [re-frame.core/debug]
 (fn [{db :db} [_ {:keys [complete] :as result}]]
   {:db         (prompts/complete db complete)
    :dispatch-n [[:logistics/parse result]
                 [:personal/parse result]
                 [:community/parse result]]}))

(reg-event-fx
 :app.initialize.fetch/fail
 (fn [{:keys [db]} [_ err]]
   {:dispatch [:app/notify (n/error "Error encountered during initialization!")]
    :db       (assoc db :app/loading false)}))

;; On navigation, update the current prompt and clear any notifications
(reg-event-db
 :app/nav
 (fn [db [_ prompt-key]]
   (assoc db :prompt/current prompt-key :app/notifications [])))

(reg-event-db
 :app/notify
 (fn [db [_ n-or-ns]]
   (assoc db :app/notifications
          (if (sequential? n-or-ns)
            (if (vector? n-or-ns) n-or-ns (vec n-or-ns))
            [n-or-ns]))))

;; =============================================================================
;; Notifications
;; =============================================================================

(defn- remove-at [v i]
  (vec (concat (subvec v 0 i) (subvec v (inc i)))))

;; Delete a notification by index
(reg-event-db
 :notification/delete
 (fn [db [_ idx]]
   (assoc db :app/notifications (remove-at (:app/notifications db) idx))))

(reg-event-db
 :notification/clear-all
 (fn [db _]
   (assoc db :app/notifications [])))

;; Allow route changes to be expressed as events
(reg-fx
 :route
 (fn [new-route]
   (navigate! new-route)))
