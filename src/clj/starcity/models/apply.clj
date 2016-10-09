(ns starcity.models.apply
  (:require [starcity.models.apply.initialize]
            [starcity.models.apply.progress]
            [starcity.models.apply.update]
            [starcity.models.apply.payment]
            [starcity.models.apply.help]
            [starcity.models.apply.common :as common]
            [potemkin :refer [import-vars]]
            [clojure.spec :as s]))


;; =============================================================================
;; API
;; =============================================================================

(import-vars
 [starcity.models.apply.initialize initial-data]
 [starcity.models.apply.progress progress is-payment-allowed?]
 [starcity.models.apply.update update]
 [starcity.models.apply.payment submit-payment application-fee]
 [starcity.models.apply.help ask-question])

(defn locked?
  "Given an `account-id`, return `true` if the application associated w/ this
  account is locked."
  [account-id]
  (-> (common/by-account-id account-id)
      :member-application/locked
      boolean))
