(ns admin.properties.subs
  (:require [admin.properties.db :as db]
            [re-frame.core :refer [reg-sub]]
            [toolbelt.core :as tb]))

(reg-sub
 ::properties
 (fn [db _]
   (db/path db)))

(reg-sub
 :properties
 :<- [::properties]
 (fn [db _]
   (db/properties db)))

;; =============================================================================
;; Overview

(reg-sub
 :properties/overview
 :<- [::properties]
 (fn [db _]
   (let [properties (db/properties db)
         ids        (:overview-ordering db)]
     (map (partial get properties) ids))))

(reg-sub
 :properties.overview/fetching?
 :<- [::properties]
 (fn [db _]
   (db/fetching-overview? db)))

;; =============================================================================
;; Entry

(reg-sub
 :property/viewing-id
 :<- [::properties]
 (fn [db _]
   (:viewing db)))

(reg-sub
 :property/viewing
 :<- [:property/viewing-id]
 :<- [:properties]
 (fn [[property-id properties] _]
   (get properties property-id)))

(reg-sub
 :property.viewing/fetching?
 :<- [::properties]
 (fn [db _]
   (db/fetching-property? db)))

(reg-sub
 :property/name
 :<- [:property/viewing]
 (fn [property _]
   (:property/name property)))
