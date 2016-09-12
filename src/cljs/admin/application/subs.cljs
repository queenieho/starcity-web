(ns admin.application.subs
  (:require [re-frame.core :refer [reg-sub]]
            [starcity.dates :refer [format]]
            [starcity.log :as l]
            [clojure.string :as str]))

;; =============================================================================
;; Subscriptions
;; =============================================================================

;; Produces the "current application", which is simply the application that is
;; currently being viewed (determined by URL)
(reg-sub
 :application/current
 (fn [db _]
   (let [id (get-in db [:application :current-id])]
     (get-in db [:application :applications id]))))

;; Applicant's full name
(reg-sub
 :application/full-name
 :<- [:application/current]
 (fn [{name :name} _]
   name))

;; =============================================================================
;; Tabs

;; The available tabs for the current application. This *may* change depending
;; on the data available for a specific application.
(reg-sub
 :application/tabs
 :<- [:application/current]
 (fn [app _]
   ;; NOTE: Hardcoded for now
   (cond-> [:basic-info
            :move-in
            :community
            :income]
     (:pet app) (conj :pets))))

;; The tab that is currently being viewed in the application
(reg-sub
 :application/active-tab
 (fn [db _]
   (get-in db [:application :active-tab])))

;; =====================================
;; Tab Content

(reg-sub
 :application/basic-info
 :<- [:application/current]
 (fn [app _]
   (l/log app) ; TODO: Remove
   (select-keys app [:email :phone-number :completed-at :address])))

(reg-sub
 :application/move-in
 :<- [:application/current]
 (fn [app _]
   (select-keys app [:move-in :properties :term])))

(reg-sub
 :application/pets
 :<- [:application/current]
 (fn [{:keys [pet]}]
   pet))

(reg-sub
 :application/community
 :<- [:application/current]
 (fn [{:keys [community-fitness]}]
   community-fitness))

(reg-sub
 :application/income
 :<- [:application/current]
 (fn [{:keys [income]} _]
   income))
