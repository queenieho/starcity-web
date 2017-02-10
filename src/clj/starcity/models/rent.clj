(ns starcity.models.rent
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure.spec :as s]
            [datomic.api :as d]
            [starcity.models
             [account :as account]
             [member-license :as member-license]]
            [starcity.models.rent.util :refer [first-day-next-month]]
            [starcity.models.stripe.customer :as customer]
            [starcity.models.rent-payment :as rent-payment]))

;; =============================================================================
;; Next Payment

;; NOTE: There will be some weird behavior here at renewal time. If the
;; currently active license ends next month, but member has chosen to renew,
;; what then?
(defn- due-date
  "Produces the due date of the next rent payment. Produces `nil` when the
  `member-license` will have expired by the next billing cycle."
  [member-license]
  (let [ends       (c/to-date-time (member-license/ends member-license))
        next-cycle (t/plus (first-day-next-month (t/now)) (t/days 4))]
    (when-not (t/after? next-cycle ends)
      next-cycle)))

(s/fdef scheduled-date
        :args (s/cat :member-license :starcity.spec.datomic/entity)
        :ret :starcity.spec/datetime)

(defn next-payment
  ;; TODO: DOCUMENTATION
  [conn account]
  (let [license (member-license/active conn account)]
    {:amount (member-license/rate license)
     :due-by (c/to-date (due-date license))}))

;; TODO: Spec

;; =============================================================================
;; Bank Account

(defn bank-account
  [account]
  (when-let [customer (account/stripe-customer account)]
    (when (customer/has-verified-bank-account? customer)
      (let [bank-account (customer/active-bank-account customer)]
        {:bank-name (customer/account-name bank-account)
         :number    (customer/account-last4 bank-account)}))))

;; =============================================================================
;; Payments

(defn- query-payments [conn account]
  (->> (d/q '[:find [?e ...]
              :in $ ?a
              :where
              [?a :account/license ?l]
              [?l :member-license/rent-payments ?e]]
            (d/db conn) (:db/id account))
       (map (partial d/entity (d/db conn)))))

(defn payments
  "Query the 'history' of `account`'s rent payments. Pulls the last twelve
  payments sorted by most recent first."
  [conn account]
  (let [payments (query-payments conn account)]
    (->> (sort-by :rent-payment/period-start payments)
         (reverse))))

(comment
  (let [conn    starcity.datomic/conn
        account (d/entity (d/db conn) [:account/email "member@test.com"])]
    (bank-account conn account))

  (defn- rand-date []
    (let [plus-or-minus (rand-int 2)
          days          (rand-int 10)]
      (if (zero? plus-or-minus)
        (c/to-date (t/minus (t/now) (t/days days)))
        (c/to-date (t/plus (t/now) (t/days days))))))

  (sort-by :created-at
           (->> (take 10 (repeatedly rand-date))
                (map #(assoc {} :created-at %))))
  )
