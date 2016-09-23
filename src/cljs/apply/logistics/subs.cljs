(ns apply.logistics.subs
  (:require [re-frame.core :refer [reg-sub]]
            [apply.logistics.models :refer [pets-complete?]]))

;; =============================================================================
;; Subscriptions
;; =============================================================================

;; =============================================================================
;; Form Data

(reg-sub
 :logistics/communities
 (fn [db _]
   (get db :logistics/communities #{})))

(reg-sub
 :logistics/term
 (fn [db _]
   (get db :logistics/term nil)))

(reg-sub
 :logistics/move-in-date
 (fn [db _]
   (get db :logistics/move-in-date nil)))

(reg-sub
 :logistics/pets
 (fn [db _]
   (get db :logistics/pets {})))

;; =============================================================================
;; Form data complete?

(reg-sub
 :logistics.communities/complete?
 :<- [:logistics/communities]
 (fn [communities _]
   (not (empty? communities))))

(reg-sub
 :logistics.term/complete?
 :<- [:logistics/term]
 (fn [term _]
   (not (nil? term))))

(reg-sub
 :logistics.move-in-date/complete?
 :<- [:logistics/move-in-date]
 (fn [move-in-date _]
   (not (nil? move-in-date))))

;; The completion logic can also by hooked in by passing pet data as a param
(reg-sub
 :logistics.pets/complete?
 :<- [:logistics/pets]
 (fn [pet-info _]
   (pets-complete? pet-info)))
