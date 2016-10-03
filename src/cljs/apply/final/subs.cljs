;; (ns blah.core)
(ns apply.final.subs
  (:require [re-frame.core :refer [reg-sub]])
  )

(reg-sub
 :final.pay/complete?
 (fn [_ _]
   ;; TODO: (prompts/completion-stub true false)
   {:local true :remote false}))
