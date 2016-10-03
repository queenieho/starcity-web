(ns apply.prompts.subs
  (:require [re-frame.core :refer [reg-sub]]
            [apply.prompts.models :as prompts]))

;; =============================================================================
;; Navigation
;; =============================================================================

(reg-sub
 :prompt/current
 (fn [db _]
   (get db :prompt/current)))

(reg-sub
 :prompt/next
 (fn [db _]
   (let [current (get db :prompt/current)]
     (prompts/next current))))

(reg-sub
 :prompt/next-button-label
 :<- [:prompt/current]
 (fn [current-prompt _]
   (case current-prompt
     :overview/welcome          "Begin"
     :community/communal-living "Finish"
     "Next")))

(reg-sub
 :prompt/previous
 (fn [db _]
   (let [current (get db :prompt/current)]
     (prompts/previous current))))

;; =============================================================================
;; HTTP
;; =============================================================================

(reg-sub
 :prompt/is-loading
 (fn [db _]
   (get db :prompt/loading)))

(reg-sub
 :prompt/is-saving
 (fn [db _]
   (get db :prompt/saving)))

(reg-sub
 :prompt/can-save?
 (fn [db _]
   (let [curr-prompt            (get db :prompt/current)
         {:keys [local remote]} (get db curr-prompt)]
     (not= local remote))))

;; TODO: Not the best name
;; consider :prompt/payment-allowed?
(reg-sub
 :prompt/finished?
 (fn [db _]
   (get db :prompts-complete)))

;; =============================================================================
;; Help
;; =============================================================================

(reg-sub
 :prompt/help
 (fn [db _]
   (get db :prompt/help)))

(reg-sub
 :prompt.help/form-data
 :<- [:prompt/help]
 (fn [{question :question} _]
   question))

(reg-sub
 :prompt.help/loading?
 :<- [:prompt/help]
 (fn [{loading :loading} _]
   loading))

(reg-sub
 :prompt.help/can-send?
 :<- [:prompt/help]
 (fn [{question :question} _]
   (> (count question) 0)))

(reg-sub
 :prompt.help/showing?
 :<- [:prompt/help]
 (fn [{showing :showing} _]
   showing))
