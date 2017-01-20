(ns starcity.models.member-license
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure.spec :as s]
            [datomic.api :as d]
            [starcity.util :refer :all]
            [starcity.models.license :as license]
            [starcity.models.unit :as unit]
            [starcity.models.property :as property]))

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
