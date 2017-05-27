(ns admin.home.db
  (:require [cljsjs.moment]))

(def path ::home)
(def default-value
  {path {:accounts/created     0
         :applications/created 0
         :metrics/loading      false
         :referrals/loading    false
         :controls             {:from (.date (js/moment.) 1)
                                :to   (.add (js/moment.) 2 "days")}
         :referrals            []}})

(defn metrics-loading? [db]
  (:metrics/loading db))

(defn referrals-loading? [db]
  (:referrals/loading db))

(defn controls [db]
  (:controls db))

(defn referrals [db]
  (:referrals db))

(defn metrics [db]
  (select-keys db [:accounts/created :applications/created]))
