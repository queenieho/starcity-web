(ns admin.application.entry.subs
  (:require [admin.application.entry.db :refer [root-db-key]]
            [admin.application.entry.model :as model]
            [re-frame.core :refer [reg-sub]]
            [starcity.dates :as dates]
            [starcity.log :as l]
            [clojure.string :as s]
            [cljs-time.coerce :as c]
            [cljs-time.format :as f]))

;; =============================================================================
;; Internal

(reg-sub
 root-db-key
 (fn [db _]
   (get db root-db-key)))

;; Produces the "current application", which is simply the application that is
;; currently being viewed (determined by URL)
(reg-sub
 :application.entry/current
 :<- [root-db-key]
 (fn [{id :current-id, applications :applications} _]
   (get applications id)))

;; =============================================================================
;; Helpers

(def ^:private date-formatter
  (f/formatter "M/d/yyyy"))

(def ^:private format-date
  (comp (partial f/unparse date-formatter) c/to-local-date-time))

;; =============================================================================
;; UI

(reg-sub
 :application.entry/loading?
 :<- [root-db-key]
 (fn [db _]
   (:loading db)))

;; =============================================================================
;; Application

(reg-sub
 :application.entry/account-id
 :<- [:application.entry/current]
 (fn [data _]
   (:account-id data)))

(reg-sub
 :application.entry/full-name
 :<- [:application.entry/current]
 (fn [{name :name} _]
   name))

(reg-sub
 :application.entry/complete?
 :<- [:application.entry/current]
 (fn [data _]
   (:completed data)))

(reg-sub
 :application.entry/approved?
 :<- [:application.entry/current]
 (fn [data _]
   (:approved data)))

(reg-sub
 :application.entry/term
 :<- [:application.entry/current]
 (fn [data _]
   (:term data)))

(reg-sub
 :application.entry/desired-move-in
 :<- [:application.entry/current]
 (fn [data _]
   (when-let [move-in (:move-in data)]
     (format-date move-in))))

(reg-sub
 :application.entry/communities
 :<- [:application.entry/current]
 (fn [data _]
   (map :name (:communities data))))

(reg-sub
 :application.entry/pet
 :<- [:application.entry/current]
 (fn [{:keys [pet]}]
   pet))

(reg-sub
 :application.entry/community-fitness
 :<- [:application.entry/current]
 (fn [{:keys [community-fitness]}]
   community-fitness))

(reg-sub
 :application.entry/income
 :<- [:application.entry/current]
 (fn [{:keys [income]} _]
   income))

(reg-sub
 :application.entry/address
 :<- [:application.entry/current]
 (fn [{address :address} _]
   address))

;; =============================================================================
;; Menu

(reg-sub
 :application.entry.menu/info-tabs
 :<- [root-db-key]
 (fn [db _]
   (filter model/info-tab? (model/tabs db))))

(reg-sub
 :application.entry.menu/action-tabs
 :<- [root-db-key]
 (fn [db _]
   (filter model/action-tab? (model/tabs db))))

(reg-sub
 :application.entry.menu/active
 :<- [root-db-key]
 (fn [db _]
   (model/active-tab db)))

;; =====================================
;; Approval

(reg-sub
 :application.entry/approving?
 :<- [root-db-key]
 (fn [data _]
   (:approving data)))

(reg-sub
 :application.entry.approval/selected-community
 :<- [root-db-key]
 (fn [data _]
   (:selected-community data)))

(reg-sub
 :application.entry.approval/deposit-amount
 :<- [root-db-key]
 :<- [:application.entry/current]
 :<- [:application.entry.approval/selected-community]
 (fn [[data application selected] _]
   (if-let [amount (:deposit-amount data)]
     amount
     (model/base-rent application selected))))

(reg-sub
 :application.entry.approval/communities
 :<- [:application.entry/current]
 (fn [{:keys [communities]} _]
   (for [c communities]
     [(:name c) (:internal-name c)])))

(reg-sub
 :application.entry.approval/email-content
 :<- [root-db-key]
 (fn [data _]
   (:email-content data)))
