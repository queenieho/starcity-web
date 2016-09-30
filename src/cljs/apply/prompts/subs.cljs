(ns apply.prompts.subs
  (:require [re-frame.core :refer [reg-sub]]
            [apply.prompts.models :as prompts]))

;; =============================================================================
;; Prompt-related
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
 :prompt/previous
 (fn [db _]
   (let [current (get db :prompt/current)]
     (prompts/previous current))))

(reg-sub
 :prompt/loading
 (fn [db _]
   (get db :prompt/loading)))
