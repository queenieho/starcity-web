(ns starcity.events.stripe.charge
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [starcity
             [datomic :refer [conn]]
             [util :refer [chan?]]]
            [starcity.events.plumbing :refer [defproducer]]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [rent-payment :as rent-payment]
             [security-deposit :as security-deposit]]
            [starcity.models.stripe.customer :as customer]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- fetch-valid
  "Attempt to look up `charge-id` and return the charge entity. Throws an
  exception if a) the charge is not found, or b) the charge has a non-pending
  status."
  [charge-id]
  (let [charge (charge/lookup charge-id)]
    (cond
      (nil? charge)
      (throw (ex-info "Cannot find charge entity." {:charge-id charge-id}))

      (not (charge/is-pending? charge))
      (throw (ex-info "Only a pending charge can be marked as failed!"
                      {:charge    (:db/id charge)
                       :charge-id charge-id
                       :status    (charge/status charge)}))

      :otherwise charge)))

;; =============================================================================
;; Producers
;; =============================================================================

;; =============================================================================
;; Success

(defmulti succeeded (fn [conn charge _] (charge/type conn charge)))

(defn- new-amount
  "Given the security deposit and an amount in cents, determine what the new
  amount should be."
  [security-deposit amount]
  (let [amount-dollars (int (/ amount 100))]
    (+ (or (security-deposit/amount-received security-deposit) 0)
       amount-dollars)))

(defmethod succeeded :security-deposit [conn charge amount]
  (let [sd (security-deposit/by-charge charge)]
    @(d/transact conn [(charge/succeeded-tx charge)
                       {:db/id                            (:db/id sd)
                        :security-deposit/amount-received (new-amount sd amount)}])))

;; This means that we're looking at a successful `:rent-payment` entity of
;; type ACH. Simply set its status to `:rent-payment.status/paid`.
(defmethod succeeded :rent [conn charge _]
  (let [p (rent-payment/by-charge conn charge)]
    @(d/transact conn [(charge/succeeded-tx charge)
                       (rent-payment/paid p)])))

(defmethod succeeded :default [conn charge _]
  @(d/transact conn [(charge/succeeded-tx charge)]))

(defproducer succeeded! ::succeeded
  [charge-id amount]
  (let [charge (fetch-valid charge-id)]
    (succeeded conn charge amount)))

(s/fdef succeeded!
        :args (s/cat :charge-id string? :amount integer?)
        :ret chan?)

(comment
  (let [charge-id "py_19dVuEIvRccmW9nOihcbvJwF"]
    (succeeded! charge-id "Oh no! It failed!"))

  )

;; =============================================================================
;; Failure

(defmulti failed
  "Perform the necessary database updates specific to charge of `type`."
  charge/type)

(defmethod failed :security-deposit [conn charge]
  (let [sd (security-deposit/by-charge charge)]
    (when (security-deposit/is-unpaid? sd)
      ;; TODO: Make event!
      ;; Delete the customer to kick him/her back through the onboarding flow
      (customer/delete! (-> charge charge/account account/stripe-customer)))
    @(d/transact conn [(charge/failed-tx charge)])))

(defmethod failed :rent [conn charge]
  ;; The ACH payment failed -- user will need to try again.
  (let [p (rent-payment/by-charge conn charge)]
    @(d/transact conn [(rent-payment/due p)
                       (charge/failed-tx charge)
                       [:db/retract (:db/id p) :rent-payment/paid-on (:rent-payment/paid-on p)]])))

(defmethod failed :default [conn charge]
  @(d/transact conn [(charge/failed-tx charge)]))

(defproducer failed! ::failed
  [charge-id failure-message]
  (let [charge (fetch-valid charge-id)]
    (failed conn charge)))

(s/fdef failed!
        :args (s/cat :charge-id string? :message string?)
        :ret chan?)

(comment
  (let [charge-id "py_19dVUbIvRccmW9nOKj60A2i7"]
    (failed! charge-id "Oh no! It failed!"))

  )
