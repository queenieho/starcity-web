(ns starcity.webhooks.stripe.invoice
  (:require [clj-time.coerce :as c]
            [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.events.stripe.invoice :as si]
            [starcity.models.rent-payment :as rent-payment]
            [starcity.models.stripe.event :as event]
            [starcity.webhooks.stripe.common :refer :all]))

;; TODO: Think about how to deal with expiring licenses and/or renewal.

;; =============================================================================
;; Invoice Created

(defmethod handle-event "invoice.created" [data event]
  (let [invoice-id   (get-in data [:data :object :id])
        customer-id  (get-in data [:data :object :customer])
        period-start (-> (get-in data [:data :object :period_start])
                         (* 1000) ; comes in as seconds
                         c/from-long
                         c/to-date)]
    (manage event (si/created! invoice-id customer-id period-start))))

;; =============================================================================
;; Payment Failure

(defmethod handle-event "invoice.payment_failed" [data event]
  (let [invoice-id (get-in data [:data :object :id])]
    (manage event (si/payment-failed! invoice-id))))

;; =============================================================================
;; Payment Success

(defmethod handle-event "invoice.payment_succeeded" [data event]
  (let [invoice-id (get-in data [:data :object :id])]
    (manage event (si/payment-succeeded! invoice-id))))

(comment

  (let [evt (event/create "evt1" "invoice.created")]
    (handle-event {:id   "evt1"
                   :type "invoice.created"
                   :data {:object {:id           "in_00000000000000"
                                   :customer     "cus_9bzpu7sapb8g7y"
                                   :period_start 1485907200}}}
                  evt))


  ;; (map d/touch (:member-license/rent-payments (customer-id->member-license "cus_9mW5vsTnVxAEsA")))

  (let [invoice-id "in_00000000000000"]
    (rent-payment/member-license (d/entity (d/db conn) [:rent-payment/invoice-id invoice-id])))

  (let [evt (event/create "evt2" "invoice.payment_failed")]
    (handle-event {:id   "evt1"
                   :type "invoice.payment_failed"
                   :data {:object {:id       "in_00000000000000"
                                   :customer "cus_9mW5vsTnVxAEsA"}}}
                  evt))

  )
