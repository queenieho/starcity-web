(ns mars.account.subs
  (:require [re-frame.core :refer [reg-sub]]
            [mars.account.db :as db]
            [mars.account.rent.subs]))

(reg-sub
 ::account
 (fn [db _]
   (db/path db)))

(reg-sub
 :account/full-name
 :<- [::account]
 (fn [account _]
   (db/full-name account)))

(reg-sub
 :account/subsection
 :<- [::account]
 (fn [db _]
   (:subsection db)))
