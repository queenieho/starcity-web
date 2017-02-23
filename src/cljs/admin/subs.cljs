(ns admin.subs
  (:require [re-frame.core :refer [reg-sub]]
            [admin.accounts.subs]
            [admin.properties.subs]
            [admin.units.subs]
            [admin.licenses.subs]
            [admin.db :as db]
            [admin.routes :as routes]
            [clojure.string :refer [capitalize]]
            [toolbelt.core :as tb :refer [str->int]]))

;; =============================================================================
;; Nav

(reg-sub
 ::nav
 (fn [db _]
   (db/nav-path db)))

(reg-sub
 :nav/route
 :<- [::nav]
 (fn [db _]
   (:route db)))

(reg-sub
 :nav/current-page
 :<- [::nav]
 (fn [db _]
   (db/current-page db)))

;; =====================================
;; Breadcrumbs

(defn- bc
  ([path]
   (bc path (-> path name capitalize)))
  ([path label]
   (bc path label {}))
  ([path label opts]
   [(apply routes/path-for path (-> opts seq flatten)) label]))

(defmulti bcs :page)

(defmethod bcs :home [_]
  [(bc :home)])

(defmethod bcs :accounts [_]
  (conj (bcs {:page :home})
        (bc :accounts)))

(defmethod bcs :account [{:keys [params account/name]}]
  (conj (bcs {:page :accounts})
        (bc :account name {:account-id (:account-id params)})))

(defmethod bcs :properties [_]
  (conj (bcs {:page :home})
        (bc :properties)))

(defmethod bcs :property [{:keys [params property/name]}]
  (conj (bcs {:page :properties})
        (bc :property name {:property-id (:property-id params)})))

(defmethod bcs :unit [{:keys [params unit/name] :as route}]
  (conj (bcs (assoc route :page :property))
        (bc :unit name {:property-id (:property-id params)
                        :unit-id     (:unit-id params)})))

(reg-sub
 :nav/breadcrumbs
 :<- [:nav/route]
 :<- [:accounts]
 :<- [:properties]
 :<- [:units]
 (fn [[route accounts properties units :as nav] _]
   (let [{:keys [unit-id
                 property-id
                 account-id]} (:params route)
         account-name         (:account/name (get accounts (str->int account-id)))
         property-name        (:property/name (get properties (str->int property-id)))
         unit-name            (:unit/name (get units (str->int unit-id)))]
     (bcs (assoc route
                :account/name account-name
                :property/name property-name
                :unit/name unit-name)))))

;; =====================================
;; Menu

(reg-sub
 :menu/selected-item
 :<- [::nav]
 (fn [db _]
   (db/selected-menu-item db)))

(reg-sub
 :menu/items
 :<- [::nav]
 (fn [db _]
   (db/menu-items db)))
