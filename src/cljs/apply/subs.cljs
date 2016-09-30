(ns apply.subs
  (:require [apply.prompts.subs]
            [apply.logistics.subs]
            [apply.personal.subs]
            [apply.community.subs]
            [re-frame.core :refer [reg-sub]]))

;; =============================================================================
;; App-wide
;; =============================================================================

(reg-sub
 :app/notifications
 (fn [db _]
   (get db :app/notifications)))
