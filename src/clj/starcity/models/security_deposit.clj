(ns starcity.models.security-deposit
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [starcity
             [datomic :refer [conn]]
             [util :refer :all]]
            [starcity.models
             [check :as check]
             [stripe :as stripe]]))

;; =============================================================================
;; Actions

(defn- sum-checks [security-deposit]
  (reduce
   (fn [acc check]
     (if (= (:check/status check) :check.status/cleared)
       (+ acc (:check/amount check))
       acc))
   0
   (:security-deposit/checks security-deposit)))

(defn- sum-charges [security-deposit]
  (->> (map stripe/fetch-charge (:security-deposit/charges security-deposit))
       (reduce
        (fn [acc {:keys [status amount]}]
          (if (= status "succeeded")
            (+ acc (/ amount 100))
            acc))
        0)))

;; NOTE: There's a potential for this to get out-of-sync in the admin dashboard
;; due to the factoring in of the previous amount received. Ideally it would
;; only be calculated from the sum of checks and charges, but that's not
;; possible currently due to historic payments w/ things like bill.com. A
;; solution would be to inspect the tx history of the check entity to see if
;; it's going from cleared to not cleared.
(defn- new-amount-received
  [security-deposit check]
  (int (+ (sum-checks security-deposit)
          (sum-charges security-deposit)
          (:security-deposit/amount-received security-deposit)
          (if (= (check/status check) check/cleared)
            (check/amount check)
            0))))

(defn add-check
  "Add a new `check` to the `security-deposit` entity, taking into consideration
  the check's contribution to the total amount received."
  [security-deposit check]
  (let [amount-received (new-amount-received security-deposit check)]
    {:db/id                            (:db/id security-deposit)
     :security-deposit/checks          [check]
     :security-deposit/amount-received amount-received}))

(s/fdef add-check
        :args (s/cat :security-deposit entity?
                     :check check/check?)
        :ret (s/keys :req [:db/id
                           :security-deposit/checks
                           :security-deposit/amount-received]))

(defn update-check
  [security-deposit check]
  {:db/id                            (:db/id security-deposit)
   :security-deposit/amount-received (new-amount-received security-deposit check)})

(s/fdef update-check
        :args (s/cat :security-deposit entity?
                     :check check/updated?)
        :ret (s/keys :req [:db/id
                           :security-deposit/amount-received]))

;; =============================================================================
;; Selectors

(def amount-received :security-deposit/amount-received)
(def amount-required :security-deposit/amount-required)
(def due-by :security-deposit/due-by)
(def account :security-deposit/account)

(defn amount-remaining [security-deposit]
  (- (amount-required security-deposit) (amount-received security-deposit)))

(defn- amount-pending-checks [security-deposit]
  (->> (:security-deposit/checks security-deposit)
       (filter #(or (= (:check/status %) :check.status/received)
                    (= (:check/status %) :check.status/deposited)))
       (reduce #(+ %1 (:check/amount %2)) 0)))


(defn- amount-pending-charges [security-deposit]
  (letfn [(-cents [amt] (float (/ amt 100)))]
    (->> (:security-deposit/charges security-deposit)
         (filter #(= (:charge/status %) :charge.status/pending))
         (map (comp -cents :amount stripe/fetch-charge))
         (reduce + 0))))

(defn amount-pending
  "Using attached checks and charges, determine how much is in a pending state.
  This means a) checks that have not cleared and b) charges that are in a
  pending state."
  [security-deposit]
  (apply + ((juxt amount-pending-checks
                  amount-pending-charges) security-deposit)))

;; =============================================================================
;; Predicates

(defn is-unpaid? [security-deposit]
  (= 0 (get security-deposit :security-deposit/amount-received 0)))

(def is-paid? (comp not is-unpaid?))

(defn paid-in-full? [security-deposit]
  (and (is-paid? security-deposit)
       (>= (amount-received security-deposit)
           (amount-required security-deposit))))

(defn partially-paid? [security-deposit]
  (and (not (paid-in-full? security-deposit))
       (is-paid? security-deposit)))

;; =============================================================================
;; Queries

(defn by-account [account]
  (->> (d/q '[:find ?e .
              :in $ ?a
              :where [?e :security-deposit/account ?a]]
            (d/db conn) (:db/id account))
       (d/entity (d/db conn))))

(defn by-charge [charge]
  (->> (d/q '[:find ?e .
              :in $ ?c
              :where [?e :security-deposit/charges ?c]]
            (d/db conn) (:db/id charge))
       (d/entity (d/db conn))))
