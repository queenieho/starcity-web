(ns starcity.webhooks.stripe
  (:require [datomic.api :as d]
            [ring.util.response :as response]
            [starcity.datomic :refer [conn]]
            [starcity.models
             [charge :as charge]
             [security-deposit :as deposit]]
            [starcity.models.stripe.event :as event]
            [taoensso.timbre :as timbre]))

;; =============================================================================
;; Internal
;; =============================================================================

(def ^:private event-id :id)
(def ^:private event-type :type)

(defmulti handle-event (fn [event-data _] (:type event-data)))

(defmethod handle-event "charge.succeeded" [event-data event]
  ;; Get the charge id and retrieve the charge from db
  (let [{charge-id :id amount :amount} (get-in event-data [:data :object])
        charge-ent                     (charge/lookup charge-id)]
    ;; Determine whether it's a security deposit charge
    (when-let [security-deposit (deposit/is-security-deposit-charge? charge-ent)]
      ;; If so, we want to indicate that the security deposit is successfully paid.
      (deposit/paid security-deposit charge-ent amount))
    ;; Regardless, mark the charge as succeeded
    (charge/succeeded charge-ent)
    ;; Finally, indicate that the event was successfully processed
    (event/succeeded event)))

(defmethod handle-event "charge.failed" [event-data event]
  (let [charge-id   (get-in event-data [:data :object :id])
        failure-msg (get-in event-data [:data :object :failure_message])
        charge-ent  (charge/lookup charge-id)
        acct        (:charge/account charge-ent)]
    (timbre/infof "[STRIPE WEBHOOK] ACH Charge failed for: charge-id - %s, account: %s"
                  charge-id (:account/email acct))
    ;; When this charge is attached to a security deposit...
    (when-let [security-deposit (deposit/is-security-deposit-charge? charge-ent)]
      ;; And the deposit is completely unpaid...
      (when (deposit/is-unpaid? security-deposit)
        (deposit/ach-charge-failed acct failure-msg)))
    ;; Mark the charge as failed
    (charge/failed charge-ent)
    ;; Indicate that the event was handled successfully
    (event/succeeded event)))

;; If the bank account could not be verified because either of the two small
;; deposits failed, you will receive a customer.source.updated notification. The
;; bank account’s status will be set to verification_failed.
;; https://stripe.com/docs/ach#ach-specific-webhook-notifications
(defmethod handle-event "customer.source.updated" [event-data event]
  (let [status      (get-in event-data [:data :object :status])
        customer-id (get-in event-data [:data :object :customer])]
    (when (= status "verification_failed")
      (let [customer (d/entity (d/db conn) [:stripe-customer/customer-id customer-id])]
        (timbre/infof "[STRIPE WEBHOOK] Microdeposit verification failed for %s"
                      (-> customer :stripe-customer/account :account/email))
        (deposit/microdeposit-verification-failed customer)))
    ;; Indicate that the event was handled successfully
    (event/succeeded event)))

;; An event that we don't have any specific logic to handle.
(defmethod handle-event :default [event-data entity]
  ;; Log it
  (timbre/info "[STRIPE WEBHOOK] Received unhandled event." event-data)
  ;; Mark it as successful
  (event/succeeded entity))

(defn process [{params :params :as req}]
  ;; First, see if there's an event that exists.
  (if-let [e (event/lookup (event-id params))]
    ;; There is, so we'll only want to process it when it's failed
    (when (event/failed? e)
      (event/processing e)                ; indicate that we're going to work on it
      (handle-event params))
    ;; There is not, so this is a new event
    (let [event-entity (event/create (event-id params) (event-type params))]
      ;; Now we'll try to fetch it and process it. If an exception is
      ;; encountered at any point, we'll set it to failed and log.
      (try
        (let [event-data (event/fetch (event-id params))]
          (handle-event event-data event-entity))
        (catch Exception e
          (timbre/errorf e "[STRIPE WEBHOOK] Error occurred while processing event with: id - '%s', type - '%s'"
                         (event-id params) (event-type params))
          (event/failed event-entity))))))

;; =============================================================================
;; API
;; =============================================================================

(defn hook
  [{params :params :as req}]
  ;; Handle the event in the background
  ;; see https://stripe.com/docs/webhooks#best-practices
  (future (process req))
  ;; Acknowledge that we've received the event.
  (response/response {}))
