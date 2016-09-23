(ns apply.events
  (:require [apply.db :refer [default-value]]
            [apply.prompts.events]
            [re-frame.core :refer [reg-event-db reg-fx]]
            [apply.routes :refer [navigate!]]))

;; =============================================================================
;; App Events
;; =============================================================================

(reg-event-db
 :app/initialize
 (fn [_ _]
   default-value))

;; On navigation, update the current prompt and clear any notifications
(reg-event-db
 :app/nav
 (fn [db [_ prompt-key]]
   (assoc db :prompt/current prompt-key :notifications [])))

(defn- remove-at [v i]
  (vec (concat (subvec v 0 i) (subvec v (inc i)))))

(reg-event-db
 :app/notify
 (fn [db [_ n-or-ns]]
   (assoc db :notifications
          (if (sequential? n-or-ns)
            (if (vector? n-or-ns) n-or-ns (vec n-or-ns))
            [n-or-ns]))))

;; =============================================================================
;; Notifications
;; =============================================================================

;; Delete a notification by index
(reg-event-db
 :notification/delete
 (fn [db [_ idx]]
   (assoc db :notifications (remove-at (:notifications db) idx))))

;; Allow route changes to be expressed as events
(reg-fx
 :route
 (fn [new-route]
   (navigate! new-route)))
