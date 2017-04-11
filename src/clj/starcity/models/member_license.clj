(ns starcity.models.member-license
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure.spec :as s]
            [dire.core :refer [with-postcondition!]]
            [datomic.api :as d]
            [starcity.models
             [license :as license]
             [property :as property]
             [rent-payment :as rent-payment]
             [unit :as unit]]
            [starcity.util :refer [beginning-of-day end-of-day]]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Selectors

(def rate :member-license/rate)
(def payments :member-license/rent-payments)
(def commencement :member-license/commencement)
(def term (comp :license/term :member-license/license))
(def ends :member-license/ends)
(def unit :member-license/unit)
(def customer :member-license/customer)
(def subscription-id :member-license/subscription-id)
(def plan-id :member-license/plan-id)
(def account :account/_license)

(defn managed-account-id
  "Retrieve the id of the managed Stripe account for the property that
  `member-license` is a part of."
  [member-license]
  (-> member-license unit unit/property property/managed-account-id))

;; =============================================================================
;; Queries

(defn active
  "Retrieve the active license for `account`. Throws an exception if there are
  more than one active license."
  [conn account]
  (let [ls (d/q '[:find [?e ...]
                  :in $ ?a
                  :where
                  [?a :account/license ?e]
                  [?e :member-license/status :member-license.status/active]]
                (d/db conn) (:db/id account))]
    (cond
      (> (count ls) 1) (throw (ex-info "Invalid state: account has multiple active member licenses."
                                       {:account (:db/id account)}))
      (empty? ls)      (throw (ex-info "Invalid state: account has no active member licenses."
                                       {:account (:db/id account)}))
      :otherwise       (d/entity (d/db conn) (first ls)))))

(s/fdef active
        :args (s/cat :conn p/conn? :account p/entity?)
        :ret p/entity?)

(defn by-subscription-id
  "Retrieve a license given the Stripe `subscription-id`."
  [conn sub-id]
  (d/entity (d/db conn) [:member-license/subscription-id sub-id]))

(defn by-customer-id
  "Retrieve a license given the Stripe `customer-id`."
  [conn customer-id]
  (->> (d/q '[:find ?ml .
              :in $ ?sc
              :where
              [?sc :stripe-customer/account ?a]
              [?a :account/license ?ml]]
            (d/db conn) [:stripe-customer/customer-id customer-id])
       (d/entity (d/db conn))))

(defn by-invoice-id
  "Retreive a license given a Stripe `invoice-id`."
  [conn invoice-id]
  (->> (d/q '[:find ?e .
              :in $ ?i
              :where
              [?rp :rent-payment/invoice-id ?i]
              [?e :member-license/rent-payments ?rp]]
            (d/db conn) invoice-id)
       (d/entity (d/db conn))))

(defn total-late-payments
  "Return the total number of late payments that have been made by `account` in
  their current member license."
  [conn license]
  (let [payments (:member-license/rent-payments license)]
    (->> (filter #(= (rent-payment/status %) :rent-payment.status/paid)
                 payments)
         (reduce
          (fn [acc payment]
            (let [paid-on  (c/to-date-time (rent-payment/paid-on payment))
                  due-date (c/to-date-time (rent-payment/due-date payment))]
              (if (t/after? paid-on due-date)
                (inc acc)
                acc)))
          0))))

;; TODO: Replace this with a configurable number of allowed payments on the
;; member license so that we can tune it.
(def ^:private max-late-payments
  "The total number of late payments that can be associated with an account
  within a year before late fees are charged."
  1)

(defn grace-period-over?
  "Has the maximum number of allowed late payments been exceeded?"
  [conn license]
  (>= (total-late-payments conn license) max-late-payments))

(defn- payments-within
  [conn member-license date]
  (d/q '[:find [?p ...]
         :in $ ?m ?date
         :where
         [?m :member-license/rent-payments ?p]
         [?p :rent-payment/period-start ?start]
         [?p :rent-payment/period-end ?end]
         [(.after ^java.util.Date ?end ?date)]
         [(.before ^java.util.Date ?start ?date)]]
       (d/db conn) (:db/id member-license) date))

(with-postcondition! #'payments-within
  "There can be only one rent payment due in a given month."
  :at-most-one
  (fn [result]
    (< (count result) 2)))

(defn payment-within
  "Produce the rent payment entity that corresponds to the calendar
  month of `date` that belongs to `member-license`."
  [conn member-license date]
  (when-let [p (first (payments-within conn member-license date))]
    (d/entity (d/db conn) p)))

(s/fdef payment-within
        :args (s/cat :conn p/conn? :member-license p/entity? :within inst?)
        :ret (s/or :nothing nil? :payment p/entity?))

;; NOTE: Using a UTC date here won't work, as its notion of *now* is incorrect.
(defn current-payment
  "Produce `account`'s rent payment for the current pay period (this month)."
  [conn member-license]
  (if-let [p (payment-within conn member-license (java.util.Date.))]
    p
    (throw (ex-info "No current payment for this license."
                    {:member-license (:db/id member-license)}))))

;; =============================================================================
;; Predicates

(defn autopay-on?
  "Does this license have autopay on?"
  [member-license]
  (boolean
   (and (plan-id member-license)
        (subscription-id member-license))))

(s/fdef autopay-on?
        :args (s/cat :member-license p/entity?)
        :ret boolean?)

(def bank-linked?
  "Is there a bank account linked to this member license?"
  (comp boolean :stripe-customer/bank-account-token customer))

;; =============================================================================
;; Transactions

(defn add-rent-payments
  [license & payments]
  {:db/id                        (:db/id license)
   :member-license/rent-payments payments})

(defn remove-subscription
  "Retract the subscription id from the `license`."
  [license]
  [:db/retract
   (:db/id license)
   :member-license/subscription-id
   (subscription-id license)])

(defn- end-date
  [license start]
  (let [term (license/term license)]
    (->> (t/plus (c/to-date-time start) (t/months term))
         (c/to-date)
         end-of-day)))

(s/def :member-license/status
  #{:member-license.status/active
    :member-license.status/inactive
    :member-license.status/renewal
    :member-license.status/canceled})

(defn create
  "Create a new member license for from a base `license`, active for `unit`,
  with provided `commencement` date (move-in), `rate` (monthly rent) and license
  `status`."
  [license unit commencement rate status]
  {:member-license/license      (:db/id license)
   :member-license/rate         rate
   :member-license/status       status
   :member-license/commencement (beginning-of-day commencement)
   :member-license/unit         (:db/id unit)
   :member-license/ends         (end-date license commencement)})

(s/fdef create
        :args (s/cat :license p/entity?
                     :unit p/entity?
                     :commencement? inst?
                     :rate float?
                     :status :member-license/status)
        :ret (s/keys :req [:member-license/license
                           :member-license/rate
                           :member-license/status
                           :member-license/commencement
                           :member-license/unit
                           :member-license/ends]))
