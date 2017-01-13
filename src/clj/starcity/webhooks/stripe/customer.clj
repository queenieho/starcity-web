(ns starcity.webhooks.stripe.customer
  (:require [starcity.events.stripe.customer :as customer]
            [starcity.models.stripe.event :as event]
            [starcity.webhooks.stripe.common :refer :all]))

;; If the bank account could not be verified because either of the two small
;; deposits failed, you will receive a customer.source.updated notification. The
;; bank account’s status will be set to verification_failed.
;; https://stripe.com/docs/ach#ach-specific-webhook-notifications
(defmethod handle-event "customer.source.updated" [event-data event]
  (let [status      (get-in event-data [:data :object :status])
        customer-id (get-in event-data [:data :object :customer])]
    (if (= status "verification_failed")
      (manage event (customer/verification-failed! customer-id))
      ;; Otherwise, it's successful.
      (event/succeeded event))))
