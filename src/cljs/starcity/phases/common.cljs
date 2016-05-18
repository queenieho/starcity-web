(ns starcity.phases.common
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [starcity.routes :as routes]))

;; =============================================================================
;; API

(defn navigate!
  ([phase-id k]
   (navigate! phase-id (namespace k) (name k)))
  ([phase-id section group]
   (letfn [(k->s [k?]
             (if (string? k?) k? (name k?)))]
     (routes/navigate!
      (routes/phase {:phase-id phase-id
                     :section  (k->s section)
                     :group    (k->s group)})))))

;; =============================================================================
;; Subscriptions

(register-sub
 :phase/current-phase
 (fn [db _]
   (reaction (get-in @db [:location :params :phase-id]))))

(register-sub
 :phase/current-section
 (fn [db _]
   (reaction (keyword (get-in @db [:location :params :section])))))

(register-sub
 :phase/current-group
 (fn [db _]
   (reaction (keyword (get-in @db [:location :params :group])))))
