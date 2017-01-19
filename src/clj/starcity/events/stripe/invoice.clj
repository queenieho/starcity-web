(ns starcity.events.stripe.invoice
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure.core.async :refer [go]]
            [clojure.spec :as s]
            [dire.core :refer [with-pre-hook!]]
            [starcity
             [datomic :refer [conn]]
             [util :refer [chan?]]]
            [starcity.events.plumbing :refer [defproducer]]
            [starcity.models
             [member-license :as member-license]
             [rent-payment :as rent-payment]]
            [taoensso.timbre :as timbre]
            [datomic.api :as d]))

;; =============================================================================
;; Internal
;; =============================================================================

(defn- entities
  [conn invoice-id]
  (let [payment (rent-payment/by-invoice-id conn invoice-id)
        license (rent-payment/member-license payment)
        account (member-license/account license)]
    {:payment payment :license license :account account}))

;; =============================================================================
;; Invoice Created

(defn- add-rent-payment!
  "Add a new rent payment entity to member's `license` based on `invoice-id`."
  [conn invoice-id customer-id period-start]
  (let [license (member-license/by-customer-id conn customer-id)
        payment (rent-payment/autopay-payment invoice-id
                                              period-start
                                              (member-license/rate license))]
    @(d/transact conn [(member-license/add-rent-payments license payment)])))

(defproducer created! ::created
  [invoice-id customer-id period-start]
  (add-rent-payment! conn invoice-id customer-id period-start))

(s/fdef created!
        :args (s/cat :invoice-id string? :customer-id string? :period-start inst?)
        :ret chan?)

(comment
  (created! "in_19ximtjdow24tc1az91yfza4" "cus_9bzpu7sapb8g7y" (c/to-date (t/date-time 2017 1 1)))

  )

;; =============================================================================
;; Invoice Payment Failed

(defn- maybe-remove-paid-on
  "Remove the `:rent-payment/paid-on` fact iff there are both a paid-on date
  and the payment has been attempted thrice."
  [payment num-failures tx-data]
  (let [paid-on (:rent-payment/paid-on payment)]
    (if (and paid-on (= num-failures 3))
      (conj tx-data [:db/retract (:db/id payment) :rent-payment/paid-on paid-on])
      tx-data)))

(defn- failed
  "Keep track of then number of times that a payment has failed and change the
  status back to `due` when the payment has failed thrice."
  [conn invoice-id payment failures]
  (let [tx-data [(-> {:db/id                         (:db/id payment)
                     :rent-payment/autopay-failures failures}
                    (merge (when (= 3 failures)
                             {:rent-payment/status :rent-payment.status/due})))]]
    @(d/transact conn (maybe-remove-paid-on payment failures tx-data))))

(defproducer payment-failed! ::payment-failed
  [invoice-id]
  (let [payment      (rent-payment/by-invoice-id conn invoice-id)
        num-failures (-> payment rent-payment/failures inc)]
    (failed conn invoice-id payment num-failures)))

(s/fdef payment-failed!
        :args (s/cat :invoice-id string?)
        :ret chan?)

(comment
  (payment-failed! "in_19ximtjdow24tc1az91yfza4")

  )

;; =============================================================================
;; Invoice Payment Succeeded

;; An invoice payment has succeeded on Stripe -- update the corresponding
;; `rent-payment` entity and send appropriate notifications.
(defproducer payment-succeeded! ::payment-succeeded
  [invoice-id]
  (let [payment (rent-payment/by-invoice-id conn invoice-id)]
    @(d/transact conn [(rent-payment/paid payment)])))

(s/fdef payment-succeeded!
        :args (s/cat :invoice-id string?))

(comment
  (payment-succeeded! "in_19ximtjdow24tc1az91yfza4")

  )
