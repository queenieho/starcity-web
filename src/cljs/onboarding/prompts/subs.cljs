(ns onboarding.prompts.subs
  (:require [onboarding.db :as db]
            [re-frame.core :refer [reg-sub]]
            [clojure.string :as string]))

;; =============================================================================
;; Global
;; =============================================================================

(def ^:private titles
  {:deposit/method
   "How would you like to pay your security deposit?"

   :deposit.method/bank
   "Please enter your bank account information."

   :deposit.method/verify
   "Please confirm the two amounts that have been deposited."

   :deposit/pay
   "We provide two options for paying your deposit."

   :services/moving
   "Need some help moving your belongings in?"

   :services/storage
   "Do you have belongings that you don't use often but can't part with?"

   :services/customization
   "Need some help adding pizazz to your room?"

   :services/cleaning
   "Would you like to have your room cleaned weekly?"})

(reg-sub
 :prompt/title
 :<- [:menu/active]
 (fn [active _]
   (get titles active (str "TODO: Implement title for " active))))

(reg-sub
 :prompt/active
 :<- [:db]
 :<- [:menu/active]
 (fn [[db active] _]
   (assoc (get db active) :keypath active)))

(reg-sub
 :prompt/complete?
 :<- [:prompt/active]
 (fn [prompt _]
   (get prompt :complete)))

(reg-sub
 :prompt/saving?
 (fn [db _]
   (:saving db)))

(reg-sub
 :prompt/dirty?
 :<- [:prompt/active]
 (fn [prompt _]
   (:dirty prompt)))

;; =============================================================================
;; Navigation
;; =============================================================================

;; =============================================================================
;; Advance

(reg-sub
 :prompt/can-advance?
 :<- [:prompt/active]
 (fn [prompt _]
   (db/can-advance? prompt)))

;; =============================================================================
;; Retreat

(reg-sub
 :prompt/has-previous?
 :<- [:db]
 :<- [:menu/active]
 (fn [[db keypath] _]
   (not (nil? (db/previous-prompt db keypath)))))

;; =============================================================================
;; Prompt-specific
;; =============================================================================

(reg-sub
 :deposit/payment-method
 (fn [db _]
   (get-in db [:deposit/method :data :method])))

(reg-sub
 :deposit.pay/amount
 (fn [db _]
   (get-in db [:deposit/pay :rent-amount])))
