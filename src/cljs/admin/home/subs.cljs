(ns admin.home.subs
  (:require [admin.home.db :as db]
            [re-frame.core :refer [reg-sub]]
            [toolbelt.core :as tb]))

;; =============================================================================
;; Root
;; =============================================================================

(reg-sub
 ::home
 (fn [db _]
   (db/path db)))

;; =============================================================================
;; Controls
;; =============================================================================

(reg-sub
 :home/controls
 :<- [::home]
 (fn [db _]
   (db/controls db)))

;; =============================================================================
;; Metrics
;; =============================================================================

(reg-sub
 :home/metrics
 :<- [::home]
 (fn [db _]
   (db/metrics db)))

(reg-sub
 :home.metrics/loading?
 :<- [::home]
 (fn [db _]
   (db/metrics-loading? db)))

;; =============================================================================
;; Referrals
;; =============================================================================

(reg-sub
 :home/referrals
 :<- [::home]
 (fn [db _]
   (tb/log db)
   (db/referrals db)))

(reg-sub
 :home.referrals/loading?
 :<- [::home]
 (fn [db _]
   (db/referrals-loading? db)))
