(ns mars.account.rent.link-account.authorize.subs
  (:require [re-frame.core :refer [reg-sub]]
            [mars.account.rent.link-account.authorize.db :as db]
            [starcity.log :as l]))

(reg-sub
 ::authorize
 (fn [db _]
   (db/path db)))

(reg-sub
 :rent.link-account.authorize/plan
 :<- [::authorize]
 (fn [db _]
   {:rent-amount  (db/rent-amount db)
    :start-date   (db/start-date db)
    :move-in      (db/commencement-date db)
    :license-term (db/term db)
    :end-date     (db/end-date db)}))

(reg-sub
 :rent.link-account.authorize/acknowledged?
 :<- [::authorize]
 (fn [db _]
   (db/authorized? db)))

(reg-sub
 :rent.link-account.authorize/subscribing?
 :<- [::authorize]
 (fn [db _]
   (db/subscribing? db)))
