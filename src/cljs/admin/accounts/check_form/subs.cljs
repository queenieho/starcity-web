(ns admin.accounts.check-form.subs
  (:require [admin.accounts.check-form.db :as db]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::check-form
 (fn [db _]
   (db/path db)))

(reg-sub
 :check-form/title
 :<- [::check-form]
 (fn [db _]
   (if (= :update (db/check-type (:params db)))
     "Update Check"
     "Add a Check")))

(reg-sub
 :check-form/showing?
 :<- [::check-form]
 (fn [db _]
   (:showing db)))

(reg-sub
 :check-form/form-data
 :<- [::check-form]
 (fn [db _]
   (:form db)))

(def check-ks
  [:number :name :status :amount :date :received-on :bank])

(reg-sub
 :check-form/can-submit?
 :<- [:check-form/form-data]
 (fn [data _]
   (->> ((apply juxt check-ks) data)
        (every? (comp not nil?)))))

(reg-sub
 :check-form/submitting?
 :<- [::check-form]
 (fn [db _]
   (:submitting db)))
