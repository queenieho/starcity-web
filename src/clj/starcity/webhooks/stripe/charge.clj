(ns starcity.webhooks.stripe.charge
  (:require [starcity.events.stripe.charge :as charge]
            [starcity.webhooks.stripe.common :refer :all]))

;;; Successful Charge

(defmethod handle-event "charge.succeeded" [event-data event]
  ;; Get the charge id and retrieve the charge from db
  (let [{charge-id :id amount :amount} (get-in event-data [:data :object])]
    (manage event (charge/succeeded! charge-id amount))))

;;; Failed Charge

(defmethod handle-event "charge.failed" [event-data event]
  (let [{charge-id :id msg :failure_message} (get-in event-data [:data :object])]
    (manage event (charge/failed! charge-id msg))))
