(ns apply.prompts.subs
  (:require [re-frame.core :refer [reg-sub]]))

;; =============================================================================
;; Prompt-related
;; =============================================================================

(reg-sub
 :prompt/current
 (fn [db _]
   (get db :prompt/current)))

(reg-sub
 :prompt/loading
 (fn [db _]
   (get db :prompt/loading)))
