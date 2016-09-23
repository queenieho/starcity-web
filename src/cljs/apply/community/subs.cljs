(ns apply.community.subs
  (:require [apply.community.models :as m]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 :community/why-starcity
 (fn [db _]
   (get db :community/why-starcity)))

(reg-sub
 :community.why-starcity/complete?
 :<- [:community/why-starcity]
 (fn [why-starcity _]
   (not-empty why-starcity)))

(reg-sub
 :community/about-you
 (fn [db _]
   (get db :community/about-you {})))

(reg-sub
 :community.about-you/complete?
 :<- [:community/about-you]
 (fn [about-you _]
   (m/about-you-complete? about-you)))

(reg-sub
 :community/communal-living
 (fn [db _]
   (get db :community/communal-living {})))

(reg-sub
 :community.communal-living/complete?
 :<- [:community/communal-living]
 (fn [communal-living _]
   (m/communal-living-complete? communal-living)))
