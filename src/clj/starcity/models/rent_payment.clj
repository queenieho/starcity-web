(ns starcity.models.rent-payment
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [dire.core :refer [with-postcondition!]]
            [clojure.spec :as s]
            [datomic.api :as d]
            [starcity.util :refer :all]
            [plumbing.core :refer [assoc-when]]))

;; =============================================================================
;; Specs

(declare check ach autopay other)

(s/def ::method #{check ach autopay other})
(s/def ::status #{:rent-payment.status/due
                  :rent-payment.status/pending
                  :rent-payment.status/paid})

(def check :rent-payment.method/check)
(def autopay :rent-payment.method/autopay)
(def ach :rent-payment.method/ach)
(def other :rent-payment.method/other)

;; =============================================================================
;; Selectors

(def member-license :member-license/_rent-payments)
(def amount :rent-payment/amount)
(def period-start :rent-payment/period-start)
(def period-end :rent-payment/period-end)
(def status :rent-payment/status)

(defn failures [payment]
  (get payment :rent-payment/autopay-failures 0))

;; =============================================================================
;; Predicates

(defn unpaid?
  "Is `payment` unpaid?"
  [payment]
  (#{:rent-payment.status/due} (status payment)))

;; =============================================================================
;; Transactions

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
  [amount period-start period-end status & {:keys [invoice-id method due-date check paid-on
                                                   desc]}]
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
            :method autopay
            :invoice-id invoice-id)))

;; =============================================================================
;; Lookups

(defn by-invoice-id [conn invoice-id]
  (d/entity (d/db conn) [:rent-payment/invoice-id invoice-id]))

(defn by-charge [conn charge]
  (->> (d/q '[:find ?e .
              :in $ ?c
              :where
              [?e :rent-payment/charge ?c]]
            (d/db conn) (:db/id charge))
       (d/entity (d/db conn))))

;; =============================================================================
;; Queries

(defn- payments-within
  [conn account within]
  (d/q '[:find [?p ...]
         :in $ ?a ?within
         :where
         [?a :account/license ?m]
         [?m :member-license/rent-payments ?p]
         [?p :rent-payment/period-start ?start]
         [?p :rent-payment/period-end ?end]
         [(.after ^java.util.Date ?end ?within)]
         [(.before ^java.util.Date ?start ?within)]]
       (d/db conn) (:db/id account) within))

(with-postcondition! #'payments-within
  "There can be only one rent payment due in a given month."
  :at-most-one
  (fn [result]
    (< (count result) 2)))

(defn payment-within
  "Produce `account`'s rent payment for the current period."
  [conn account within]
  (when-let [p (first (payments-within conn account within))]
    (d/entity (d/db conn) p)))

(s/fdef payment-within
        :args (s/cat :conn conn? :account entity? :within inst?)
        :ret (s/or :nothing nil? :payment entity?))

(comment
  (let [conn    starcity.datomic/conn
        account (d/entity (d/db conn) [:account/email "member@test.com"])]
    (payment-within conn account (java.util.Date.)))

  )
