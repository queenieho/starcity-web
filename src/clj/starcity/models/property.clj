(ns starcity.models.property
  (:refer-clojure :exclude [name])
  (:require [clojure.spec :as s]
            [starcity.models.unit :as unit]
            starcity.spec
            [datomic.api :as d]))

;; =============================================================================
;; Selectors

(def name :property/name)
(def internal-name :property/internal-name)
(def managed-account-id :property/managed-account-id)
(def ops-fee :property/ops-fee)
(def units :property/units)

;; =============================================================================
;; Spec

(s/def :property/name string?)
(s/def :property/available-on :starcity.spec/date)
(s/def :property/cover-image-url string?)
(s/def :property/description string?)

;; =============================================================================
;; Queries

(defn occupied-units
  "Produce all units that are currently occupied."
  [conn property]
  (filter (partial unit/occupied? (d/db conn)) (units property)))

(defn available-units
  "Produces all available units in `property`.

  (A unit is considered available if there is no active member license that
  references it.)"
  [conn property]
  (remove (partial unit/occupied? (d/db conn)) (units property)))

(defn total-rent
  "The total rent that can be collected from the current active member
  licenses."
  [conn property]
  (->> (d/q '[:find ?m (sum ?rate)
              :in $ ?p
              :where
              [?p :property/units ?u]
              [?m :member-license/unit ?u]
              [?m :member-license/status :member-license.status/active]
              [?m :member-license/rate ?rate]]
            (d/db conn) (:db/id property))
       (map second)
       (reduce + 0)))

(defn- amount-query
  [conn property date status]
  (->> (d/q '[:find ?py ?amount
             :in $ ?p ?now ?status
             :where
             [?p :property/units ?u]
             [?m :member-license/unit ?u]
             [?m :member-license/status :member-license.status/active]
             [?m :member-license/rent-payments ?py]
             [?py :rent-payment/amount ?amount]
             [?py :rent-payment/status ?status]
             [?py :rent-payment/period-start ?start]
             [?py :rent-payment/period-end ?end]
             [(.after ^java.util.Date ?end ?now)]
             [(.before ^java.util.Date ?start ?now)]]
           (d/db conn) (:db/id property) date status)
       (reduce #(+ %1 (second %2)) 0)))

(defn amount-collected
  "The amount in dollars that has been collected in `property` for the month
  present within `date`."
  [conn property date]
  (amount-query conn property date :rent-payment.status/paid))

(defn amount-outstanding
  "The amount in dollars that is still due in `property` for the month present
  within `date`."
  [conn property date]
  (amount-query conn property date :rent-payment.status/due))

(defn amount-pending
  "The amount in dollars that is pending in `property` for the month present
  within `date`."
  [conn property date]
  (amount-query conn property date :rent-payment.status/pending))

;; =============================================================================
;; Managed Stripe Accounts

;; (s/def ::dob :starcity.spec/datetime)

;; ;; TODO: too many params...
;; ;; TODO: Last 4 of ssn?
;; (defn create-managed-account!
;;   "Create a new managed account for `property-id`."
;;   [property-id business-name tax-id account-number routing-number first-name last-name dob]
;;   (let [res (connect/create-account!
;;              (connect/owner first-name last-name dob)
;;              (connect/business business-name tax-id)
;;              (connect/account account-number routing-number))]
;;     (if-let [e (stripe/error-from res)]
;;       (throw (ex-info "Error encountered while trying to create managed account!"
;;                       {:stripe-error e :property-id property-id}))
;;       @(d/transact conn [{:db/id                       property-id
;;                           :property/managed-account-id (:id (stripe/payload-from res))}]))))

;; (s/fdef create-managed-account!
;;         :args (s/cat :property-id :starcity.spec/lookup
;;                      :business-name string?
;;                      :tax-id string?
;;                      :account-number string?
;;                      :routing-number string?
;;                      :first-name string?
;;                      :last-name string?
;;                      :dob ::dob))
