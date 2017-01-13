(ns mars.account.events
  (:require [re-frame.core :refer [reg-event-db
                                   reg-event-fx
                                   path]]
            [mars.account.db :as db]
            [mars.account.rent.events]))

(reg-event-fx
 :account/initialize
 [(path db/path)]
 (fn [{:keys [db]} [_ subsection]]
   {:db       (assoc db :subsection (keyword subsection))
    :dispatch [:rent/bootstrap]}))
