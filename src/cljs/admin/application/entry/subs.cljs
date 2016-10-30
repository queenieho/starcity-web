(ns admin.application.entry.subs
  (:require [admin.application.entry.db :refer [root-db-key]]
            [admin.application.entry.model :as model]
            [re-frame.core :refer [reg-sub]]
            [starcity.dates :as dates]
            [starcity.log :as l]
            [clojure.string :as s]
            [cljs-time.coerce :as c]))

;; =============================================================================
;; Subscriptions
;; =============================================================================

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

;; Applicant's full name
(reg-sub
 :application.entry/full-name
 :<- [:application.entry/current]
 (fn [{name :name} _]
   name))

;; =============================================================================
;; Tabs

;; The available tabs for the current application. This *may* change depending
;; on the data available for a specific application.
(reg-sub
 :application.entry/tabs
 :<- [:application.entry/current]
 (fn [app _]
   ;; NOTE: Hardcoded for now
   (cond-> [:basic-info
            :move-in
            :community
            :income]
     (not-empty (:pet app)) (conj :pets))))

;; The tab that is currently being viewed in the application
(reg-sub
 :application.entry/active-tab
 :<-  [root-db-key]
 (fn [data _]
   (get data :active-tab)))

;; =====================================
;; Tab Content

(def ^:private format-date
  (comp (partial dates/format :medium-datetime) c/to-local-date-time))

(reg-sub
 :application.entry/basic-info
 :<- [:application.entry/current]
 (fn [app _]
   (-> (select-keys app [:email :phone-number :completed-at :address])
       (update :completed-at format-date))))

(reg-sub
 :application.entry/move-in
 :<- [:application.entry/current]
 (fn [app _]
   (-> (select-keys app [:move-in :properties :term])
       (update :move-in format-date)
       (update :properties #(->> % (map :property/name) (s/join ", ")))
       (update :term str " months"))))

(reg-sub
 :application.entry/pets
 :<- [:application.entry/current]
 (fn [{:keys [pet]}]
   pet))

(reg-sub
 :application.entry/community
 :<- [:application.entry/current]
 (fn [{:keys [community-fitness]}]
   community-fitness))

(reg-sub
 :application.entry/income
 :<- [:application.entry/current]
 (fn [{:keys [income]} _]
   income))

;; =====================================
;; Approval

(reg-sub
 :application.entry/approving?
 :<- [root-db-key]
 (fn [data _]
   (:approving data)))

(reg-sub
 :application.entry/approved?
 :<- [:application.entry/current]
 (fn [{:keys [approved]} _]
   (boolean approved)))

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
     (model/initial-deposit-amount application selected))))

(reg-sub
 :application.entry.approval/communities
 :<- [:application.entry/current]
 (fn [{:keys [properties]} _]
   (for [p properties]
     [(:property/name p) (:property/internal-name p)])))

(reg-sub
 :application.entry.approval/email-content
 :<- [root-db-key]
 (fn [data _]
   (:email-content data)))
