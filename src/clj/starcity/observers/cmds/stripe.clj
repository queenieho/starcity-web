(ns starcity.observers.cmds.stripe
  (:require [clj-time.coerce :as c]
            [datomic.api :as d]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [cmd :as cmd]
             [member-license :as member-license]
             [msg :as msg]
             [rent-payment :as rent-payment]
             [security-deposit :as deposit]]
            [starcity.services.stripe.event :as stripe-event]
            [taoensso.timbre :as timbre]
            [clojure.spec :as s]
            [toolbelt.predicates :as p]
            [clj-time.core :as t]
            [taoensso.nippy :as nippy]))

;; =============================================================================
;; API
;; =============================================================================

(defmulti handle
  (fn [conn cmd]
    (get-in cmd [:cmd/params :event-type])))

;; =============================================================================
;; Internal
;; =============================================================================

;; catch-all
(defmethod handle :default
  [conn cmd]
  (timbre/debug ::no-handler cmd)
  [])

(defn- payload [evt]
  (get-in evt [:body :data :object]))

(defn- fetch-stripe-event
  "Fetch the Stripe event with id `event-id`. In the event that this event is
  from a connected account, its `managed-account-id` will be stored in the
  `:cmd/meta` attribute of the cmd entity."
  [{event-id :cmd/id meta :cmd/meta :as cmd}]
  (payload
   (if meta
     (stripe-event/fetch event-id :managed (:managed-account-id meta))
     (stripe-event/fetch event-id))))

(def ^:private event-subject-id
  "For when we only care about the id of the Stripe entity that is the subject
  of this event."
  (comp :id fetch-stripe-event))

;; =============================================================================
;; Charge

(defn- fetch-valid
  "Attempt to look up `charge-id` and return the charge entity. Throws an
  exception if a) the charge is not found, or b) the charge has a non-pending
  status."
  [conn charge-id]
  (let [charge (charge/lookup conn charge-id)]
    (cond
      (nil? charge)
      (throw (ex-info "Cannot find charge entity." {:charge-id charge-id}))

      (not (charge/is-pending? charge))
      (throw (ex-info "Only a pending charge can processed!"
                      {:charge    (:db/id charge)
                       :charge-id charge-id
                       :status    (charge/status charge)}))

      :otherwise charge)))

;; =====================================
;; Succeeded

(defmulti succeeded charge/type)

(defmethod succeeded :default [conn charge]
  [(charge/succeeded charge)])

(defn- new-amount
  "Given the security deposit and an amount in cents, determine what the new
  amount should be."
  [deposit amount]
  (+ (or (deposit/amount-received deposit) 0) amount))

(defmethod succeeded :security-deposit [conn charge]
  (let [sd (deposit/by-charge charge)]
    [(charge/succeeded charge)
     {:db/id                            (:db/id sd)
      :security-deposit/amount-received (new-amount sd (charge/amount charge))}]))

;; This means that we're looking at a successful `:rent-payment` entity of
;; type ACH. Simply set its status to `:rent-payment.status/paid`.
(defmethod succeeded :rent [conn charge]
  (let [p (rent-payment/by-charge conn charge)]
    [(charge/succeeded charge)
     (rent-payment/paid p)]))

(defmethod handle "charge.succeeded" [conn cmd]
  (let [charge-id (event-subject-id cmd)]
    (conj (succeeded conn (fetch-valid conn charge-id))
          (msg/charge-succeeded charge-id))))

;; =====================================
;; Failed

(defmulti charge-failed
  (fn [conn charge tx]
    (charge/type conn charge)))

(defmethod charge-failed :default [conn charge tx]
  tx)

(defmethod charge-failed :security-deposit [conn charge tx]
  (let [deposit  (deposit/by-charge charge)
        customer (->> charge charge/account (account/stripe-customer (d/db conn)))]
    ;; This is used to determine if this charge corresponds to the first payment
    ;; of the security deposit.
    (cond-> tx
      ;; Delete the customer to kick him/her back through the onboarding flow
      (deposit/is-unpaid? deposit) (conj (cmd/delete-customer customer)))))

(defmethod charge-failed :rent [conn charge tx]
  ;; The ACH payment failed -- user will need to try again.
  (let [p (rent-payment/by-charge conn charge)]
    (conj tx
          (rent-payment/due p)
          ;; `:rent-payment/paid-on` is set when the payment becomes pending --
          ;; remove that timestamp.
          [:db/retract (:db/id p) :rent-payment/paid-on (:rent-payment/paid-on p)])))

(defmethod handle "charge.failed" [conn cmd]
  (let [charge-id (event-subject-id cmd)
        charge    (fetch-valid conn charge-id)]
    (->> [(msg/charge-failed charge-id)
          (charge/failed charge)]
         (charge-failed conn charge))))

;; =============================================================================
;; Customer

(defmethod handle "customer.source.updated" [conn cmd]
  (let [{:keys [id status customer]} (fetch-stripe-event cmd)
        tx [(msg/customer-source-updated customer status)]]
    (if (= status "verification_failed")
      ;; If verification has failed then the bank information needs to be
      ;; re-entered. Delete the customer.
      (conj tx (cmd/delete-customer (d/entity (d/db conn) [:stripe-customer/customer-id customer])))
      ;; Otherwise, just pass along the `msg`.
      tx)))

;; =============================================================================
;; Invoice (Autopay)

(comment
  ;; Create Autopay Invoice
  (d/transact conn [(cmd/stripe-webhook-event "evt_19LMBqIvRccmW9nOGZY7X76h" "invoice.created")])

  ;; Payment failed
  (d/transact conn [(cmd/stripe-webhook-event "evt_19LMBrIvRccmW9nO17Yd00cu" "invoice.payment_failed")])

  ;; Payment Succeeded
  (d/transact conn [(cmd/stripe-webhook-event "evt_19LMBrIvRccmW9nO17Yd00cu" "invoice.payment_succeeded")])
  )

;; =====================================
;; Created

(defn- add-rent-payment
  "Add a new rent payment to member's `license` based on `invoice-id`."
  [conn invoice-id customer-id period-start]
  (let [license (member-license/by-customer-id conn customer-id)
        payment (rent-payment/autopay-payment invoice-id
                                              period-start
                                              (member-license/rate license))]
    (member-license/add-rent-payments license payment)))

;; NOTE: Stripe dates are in seconds since 1/1/1970 -- this converts to
;; milliseconds, then to an inst
(defn- invoice-start-date
  "Produce the invoice's start date by inspecting the line items. The
  `:period_start` date on the `event-data` has proven to be unreliable; hence
  the use of the `:lines`."
  [event-data]
  (-> event-data :lines :data first :period :start (* 1000) c/from-long c/to-date))

(defmethod handle "invoice.created" [conn cmd]
  (let [{:keys [id customer] :as data} (fetch-stripe-event cmd)
        period-start                   (invoice-start-date data)]
    [(add-rent-payment conn id customer period-start)
     (msg/invoice-created id customer period-start)]))

;; =====================================
;; Updated

;; Invoices are created without an associated payment to allow us to add
;; line-items if needed. The `invoice.updated` event will fire when the charge
;; is created, which we can then use as an opportunity to create a charge on the
;; autopay payment.
;; See https://stripe.com/docs/api#invoices for reference.

(defmethod handle "invoice.updated" [conn cmd]
  (let [{:keys [id charge]} (fetch-stripe-event cmd)
        payment             (rent-payment/by-invoice-id conn id)]
    ;; When there's not already a charge for this payment, create one.
    (if-not (rent-payment/charge payment)
      [{:db/id               (:db/id payment)
        :rent-payment/charge (charge/create charge (rent-payment/amount payment))}]
      [])))

;; =====================================
;; Failed

(defn- should-retract-paid-on?
  "`:rent-payment/paid-on` should be retracted iff there are both a paid-on date
  and the payment has been attempted thrice."
  [payment num-failures]
  (and (:rent-payment/paid-on payment) (= num-failures 3)))

(defn- retract-paid-on
  [{:keys [db/id rent-payment/paid-on] :as payment}]
  [:db/retract id :rent-payment/paid-on paid-on])

(defn- invoice-failed
  "Produce tx-data that makes the failed `payment` payable again if it has
  failed for the third time."
  [conn payment]
  (let [failures (-> payment rent-payment/failures inc)
        tx-data  [(merge
                   {:db/id                         (:db/id payment)
                    :rent-payment/autopay-failures failures}
                   (when (= rent-payment/max-autopay-failures failures)
                     {:rent-payment/status :rent-payment.status/due}))]]
    (if (should-retract-paid-on? payment failures)
      (conj tx-data (retract-paid-on payment))
      tx-data)))

(s/fdef invoice-failed
        :args (s/cat :conn p/conn? :payment p/entity?)
        :ret vector?)

(defmethod handle "invoice.payment_failed" [conn cmd]
  (let [invoice-id (event-subject-id cmd)
        payment    (rent-payment/by-invoice-id conn invoice-id)]
    (conj (invoice-failed conn payment)
          (msg/invoice-payment-failed invoice-id))))

;; =====================================
;; Succeeded

(defmethod handle "invoice.payment_succeeded" [conn cmd]
  (let [invoice-id (event-subject-id cmd)
        payment    (rent-payment/by-invoice-id conn invoice-id)]
    [(rent-payment/paid payment)
     (msg/invoice-payment-succeeded invoice-id)]))

;; =============================================================================
;; Subscription (autopay)

(defmethod handle "customer.subscription.trial_will_end" [conn cmd]
  (let [sub-id  (event-subject-id cmd)
        license (member-license/by-subscription-id conn sub-id)]
    (if (nil? license)
      (throw (ex-info "License not found for subscription." {:subscription-id sub-id}))
      [(msg/autopay-will-begin license)])))

;; After three failed payment attempts, Stripe just deletes the subscription.
;; We'll delete the reference to the subscription on our end, which will require
;; the user to re-setup autopay should she/he wish to.

(defmethod handle "customer.subscription.deleted" [conn cmd]
  (let [sub-id  (event-subject-id cmd)
        license (member-license/by-subscription-id conn sub-id)]
    (if (nil? license)
      (throw (ex-info "License not found for subscription." {:subscription-id sub-id}))
      [(member-license/remove-subscription license)
       (msg/autopay-deactivated license)])))
