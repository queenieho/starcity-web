(ns mars.events
  (:require [mars.db :refer [default-value]]
            [re-frame.core :refer [reg-event-db]]))

(reg-event-db
 :app/initialize
 (fn [_ _]
   default-value))

(reg-event-db
 :nav/home
 (fn [db _]
   (assoc db :route :home)))
