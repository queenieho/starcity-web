(ns starcity.models.member-license
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure.spec :as s]
            [datomic.api :as d]
            [starcity.models
             [license :as license]
             [property :as property]
             [rent-payment :as rent-payment]
             [unit :as unit]]
            [starcity.util :refer [entity?
                                   end-of-day
                                   beginning-of-day]]))

;; =============================================================================
;; API
;; =============================================================================

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
                  [?e :member-license/active true]]
                (d/db conn) (:db/id account))]
    (cond
      (> (count ls) 1) (throw (ex-info "Invalid state: account has multiple active member licenses."
                                       {:account (:db/id account)}))
      (empty? ls)      (throw (ex-info "Invalid state: account has no active member licenses."
                                       {:account (:db/id account)}))
      :otherwise       (d/entity (d/db conn) (first ls)))))

(s/fdef active
        :args (s/cat :conn :starcity.spec.datomic/connection
                     :account :starcity.spec.datomic/entity)
        :ret :starcity.spec.datomic/entity)

(defn by-subscription-id [conn sub-id]
  (d/entity (d/db conn) [:member-license/subscription-id sub-id]))

(defn by-customer-id [conn customer-id]
  (->> (d/q '[:find ?ml .
              :in $ ?sc
              :where
              [?sc :stripe-customer/account ?a]
              [?a :account/license ?ml]]
            (d/db conn) [:stripe-customer/customer-id customer-id])
       (d/entity (d/db conn))))

(defn by-invoice-id [conn invoice-id]
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
  (> (total-late-payments conn license) 1))

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
  "Retrieve the id of the managed Stripe account for the property that `member-license` is a part of."
  [member-license]
  (-> member-license unit unit/property property/managed-account-id))

;; =============================================================================
;; Predicates

(defn subscribed? [member-license]
  (and (plan-id member-license)
       (subscription-id member-license)))

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

(defn create
  "Create a new member license for from a base `license`, active for `unit`,
  with provided `commencement` date (move-in) and `rate` (monthly rent)."
  [license unit commencement rate]
  {:member-license/license      (:db/id license)
   :member-license/rate         rate
   :member-license/active       true
   :member-license/commencement (beginning-of-day commencement)
   :member-license/unit         (:db/id unit)
   :member-license/ends         (end-date license commencement)})

(s/fdef create
        :args (s/cat :license entity?
                     :unit entity?
                     :commencement? inst?
                     :rate float?)
        :ret (s/keys :req [:member-license/license
                           :member-license/rate
                           :member-license/active
                           :member-license/commencement
                           :member-license/unit
                           :member-license/ends]))
