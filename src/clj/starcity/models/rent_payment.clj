(ns starcity.models.rent-payment
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [dire.core :refer [with-postcondition!]]
            [clojure.spec :as s]
            [datomic.api :as d]
            [starcity.util :refer :all]
            [plumbing.core :refer [assoc-when]]
            [starcity.models.check :as check]
            [toolbelt.predicates :as p]
            [starcity.models.property :as property]))

;; =============================================================================
;; Constants
;; =============================================================================

(def max-autopay-failures
  "The maximum number of times that autopay payments will be tried before the
  subscription is canceled."
  3)

;; =============================================================================
;; Specs
;; =============================================================================

(declare check ach autopay other)

(s/def ::method #{check ach autopay other})
(s/def ::status #{:rent-payment.status/due
                  :rent-payment.status/pending
                  :rent-payment.status/paid})

(def check
  "The check payment method."
  :rent-payment.method/check)

(def autopay
  "The autopay payment method."
  :rent-payment.method/autopay)

(def ach
  "The ACH payment method."
  :rent-payment.method/ach)

(def other
  "Some other payment method."
  :rent-payment.method/other)

;; =============================================================================
;; Selectors
;; =============================================================================

(def member-license :member-license/_rent-payments)
(def amount :rent-payment/amount)
(def period-start :rent-payment/period-start)
(def period-end :rent-payment/period-end)
(def status :rent-payment/status)
(def paid-on :rent-payment/paid-on)
(def due-date :rent-payment/due-date)

(def invoice
  "The id of the Stripe invoice."
  :rent-payment/invoice-id)

(def method
  "The method used to pay this payment."
  :rent-payment/method)

(def charge
  "The charge associated with this payment."
  :rent-payment/charge)

(s/fdef charge
        :args (s/cat :payment p/entity?)
        :ret (s/or :nothing nil? :charge p/entity?))

;; TODO: rename to `autopay-failures`
(defn failures
  "The number of times autopay has failed."
  [payment]
  (get payment :rent-payment/autopay-failures 0))

(def charge :rent-payment/charge)

;; =============================================================================
;; Predicates
;; =============================================================================

(defn unpaid?
  "Is `payment` unpaid?"
  [payment]
  (#{:rent-payment.status/due} (status payment)))

(defn paid?
  "Is `payment` paid?"
  [payment]
  (#{:rent-payment.status/paid} (status payment)))

(defn past-due? [payment]
  (let [due-date (-> payment due-date c/to-date-time)
        property (-> payment member-license :member-license/unit :property/_units)
        tz       (t/time-zone-for-id (property/time-zone property))]
    (and (unpaid? payment)
         (t/after? (t/to-time-zone (t/now) tz)
                   ;; Pretends that the UTC time stored is in the time zone that
                   ;; we want. This seems kind of hacky, but it works.
                   (t/from-time-zone due-date tz)))))

;; =============================================================================
;; Transactions
;; =============================================================================

(defn- set-status [to-status]
  (fn [payment]
    {:db/id               (:db/id payment)
     :rent-payment/status to-status}))

(def due (set-status :rent-payment.status/due))
(def pending (set-status :rent-payment.status/pending))
(def paid (set-status :rent-payment.status/paid))

(defn- default-due-date [start]
  (let [start (c/to-date-time start)]
    (-> (t/date-time (t/year start) (t/month start) 5)
        (c/to-date))))

(defn create
  "Create an arbitrary rent payment payment."
  [amount period-start period-end status
   & {:keys [invoice-id method due-date check paid-on desc]}]
  (when-not (is-first-day-of-month? (c/to-date-time period-start))
    (assert due-date "Due date must be supplied when the period start is not the first day of the month."))
  (when (#{paid} status)
    (assert method "If this payment has been `paid`, a `method` must be supplied."))
  (when (#{:rent-payment.method/check} method)
    (assert check "When paying by `check`, the `check` must be supplied."))
  (let [due-date (or due-date (default-due-date period-start))]
    (assoc-when
     {:rent-payment/amount       amount
      :rent-payment/period-start (beginning-of-day period-start)
      :rent-payment/period-end   (end-of-day period-end)
      :rent-payment/status       status
      :rent-payment/due-date     (end-of-day due-date)}
     :rent-payment/check check
     :rent-payment/method method
     :rent-payment/invoice-id invoice-id
     :rent-payment/paid-on paid-on
     :rent-payment/method-desc desc)))

(defn autopay-payment
  "Create an autopay payment with status `:rent-payment.status/pending`."
  [invoice-id period-start amount]
  (let [period-end (-> period-start
                       c/to-date-time
                       t/last-day-of-the-month
                       c/to-date)]
    (create amount period-start period-end :rent-payment.status/pending
            :paid-on (java.util.Date.)
            :method autopay
            :invoice-id invoice-id)))

;; =============================================================================
;; Checks

(defn- check-status->status [check-status]
  (if (#{check/received check/deposited check/cleared} check-status)
    :rent-payment.status/paid
    :rent-payment.status/due))

(defn add-check
  "Add `check` to `payment`."
  [payment check]
  (let [new-status (check-status->status (check/status check))
        paid-on    (when (= :rent-payment.status/paid new-status)
                     (check/received-on check))]
    (assoc-when
     {:db/id               (:db/id payment)
      :rent-payment/check  check
      :rent-payment/method :rent-payment.method/check
      :rent-payment/status new-status}
     :rent-payment/paid-on paid-on)))

(s/fdef add-check
        :args (s/cat :payment p/entity? :check check/check?)
        :ret (s/keys :req [:db/id
                           :rent-payment/check
                           :rent-payment/status
                           :rent-payment/method]
                     :opt [:rent-payment/paid-on]))

(defn- new-status [updated-check]
  (when-let [s (:check/status updated-check)]
    (check-status->status s)))

(defn- maybe-retract-paid-on [payment check updated-check]
  (let [paid-on (:rent-payment/paid-on payment)]
    (when (and (= (new-status updated-check) :rent-payment.status/due) paid-on)
      [:db/retract (:db/id payment) :rent-payment/paid-on paid-on])))

(defn- maybe-add-paid-on [payment check updated-check]
  (let [old-status (:rent-payment/status payment)]
    (when (and (= (new-status updated-check) :rent-payment.status/paid)
               (= old-status :rent-payment.status/due))
      [:db/add (:db/id payment) :rent-payment/paid-on
       (or (check/received-on updated-check)
           (check/received-on check))])))

(defn update-check
  [payment check updated-check]
  (->> [(assoc-when
         {:db/id (:db/id payment)}
         :rent-payment/status (new-status updated-check))
        (maybe-retract-paid-on payment check updated-check)
        (maybe-add-paid-on payment check updated-check)]
       (remove nil?)))

(s/fdef update-check
        :args (s/cat :payment p/entity?
                     :check p/entity?
                     :updated-check check/updated?)
        :ret sequential?)

;; =============================================================================
;; Queries
;; =============================================================================

(defn by-invoice-id [conn invoice-id]
  (d/entity (d/db conn) [:rent-payment/invoice-id invoice-id]))

(defn by-charge [conn charge]
  (->> (d/q '[:find ?e .
              :in $ ?c
              :where
              [?e :rent-payment/charge ?c]]
            (d/db conn) (:db/id charge))
       (d/entity (d/db conn))))
