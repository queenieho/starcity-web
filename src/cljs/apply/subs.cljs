(ns apply.subs
  (:require [apply.prompts.subs]
            [apply.overview.subs]
            [apply.logistics.subs]
            [apply.personal.subs]
            [apply.community.subs]
            [apply.final.subs]
            [re-frame.core :refer [reg-sub]]))

;; =============================================================================
;; App-wide
;; =============================================================================

(reg-sub
 :app/notifications
 (fn [db _]
   (get db :app/notifications)))
