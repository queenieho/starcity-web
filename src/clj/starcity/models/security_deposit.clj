(ns starcity.models.security-deposit
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [starcity.datomic :refer [tempid]]
            [starcity.models
             [check :as check]
             [stripe :as stripe]]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Selectors
;; =============================================================================

(s/def :security-deposit/payment-method
  #{:security-deposit.payment-method/ach
    :security-deposit.payment-method/check})

(defn amount-received
  "The amount that we've received towards this deposit so far."
  [deposit]
  (get deposit :security-deposit/amount-received 0))

(defn amount-required
  "The amount required to consider this security deposit paid."
  [deposit]
  (get deposit :security-deposit/amount-required 0))

(def received amount-received)
(def required amount-required)
(def due-by :security-deposit/due-by)
(def account :security-deposit/account)
(def checks :security-deposit/checks)
(def charges :security-deposit/charges)

(def method
  "The payment method chosen during the onboarding flow."
  :security-deposit/payment-method)

(s/fdef method
        :args (s/cat :deposit p/entity?)
        :ret :security-deposit/payment-method)

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
;; =============================================================================

(defn is-unpaid? [security-deposit]
  (and (= 0 (get security-deposit :security-deposit/amount-received 0))
       (= 0 (amount-pending security-deposit))))

(def is-paid? (comp not is-unpaid?))

(defn paid-in-full? [security-deposit]
  (and (is-paid? security-deposit)
       (>= (amount-received security-deposit)
           (amount-required security-deposit))))

(defn partially-paid? [security-deposit]
  (and (not (paid-in-full? security-deposit))
       (is-paid? security-deposit)))

;; =============================================================================
;; Transactions
;; =============================================================================

(defn- sum-charges [security-deposit]
  (or (->> (:security-deposit/charges security-deposit)
           (filter (comp #{:charge.status/succeeded} :charge/status))
           (map #(or (:charge/amount %) 0))
           (apply +))
      0))

;; =====================================
;; Checks

(defn- include-check-amount?
  "Based on `check`'s status, should its amount be included?"
  [check]
  (#{check/cleared check/received check/deposited} (check/status check)))

(defn- has-check?
  "Is `check` already part of `deposit`'s checks?"
  [deposit check]
  (some #(= (:db/id check) (:db/id %)) (checks deposit)))

(defn- resolve-checks
  [deposit new-or-updated-check]
  (if (has-check? deposit new-or-updated-check)
    ;; Replace
    (map #(if (= (:db/id %) (:db/id new-or-updated-check))
            new-or-updated-check
            %)
         (checks deposit))
    ;; Add
    (conj (checks deposit) new-or-updated-check)))

(defn- sum-checks
  "The sum of all amounts in existing checks on `security-deposit` including the
  effect of `new-or-updated-check`."
  [security-deposit new-or-updated-check]
  (->> (resolve-checks security-deposit new-or-updated-check)
       (reduce
        (fn [acc check]
          (if (include-check-amount? check)
            (+ acc (check/amount check))
            acc))
        0)))

(defn- new-amount-received
  [security-deposit check]
  (int (+ (sum-checks security-deposit check)
          (sum-charges security-deposit))))

(defn update-check
  [security-deposit check updated-check]
  (let [;; To calculate the new amount, we need at least the check's id, amount and status
        params (merge (select-keys check [:db/id :check/amount :check/status])
                      updated-check)]
    {:db/id                            (:db/id security-deposit)
     :security-deposit/amount-received (new-amount-received security-deposit params)}))

(s/fdef update-check
        :args (s/cat :security-deposit p/entity?
                     :check p/entity?
                     :updated-check check/updated?)
        :ret (s/keys :req [:db/id :security-deposit/amount-received]))

(defn add-check
  "Add a new `check` to the `security-deposit` entity, taking into consideration
  the check's contribution to the total amount received."
  [security-deposit check]
  (let [amount-received (new-amount-received security-deposit check)]
    {:db/id                            (:db/id security-deposit)
     :security-deposit/checks          check
     :security-deposit/amount-received amount-received}))

(s/fdef add-check
        :args (s/cat :security-deposit p/entity?
                     :check check/check?)
        :ret (s/keys :req [:db/id
                           :security-deposit/checks
                           :security-deposit/amount-received]))

(defn create
  "Produce transaction data to create a security deposit entity for `account`.

  Only requires an `amount` (and `account` of course), since other details are
  filled in by `account` during the onboarding flow."
  [account amount]
  {:db/id                            (tempid)
   :security-deposit/account         (:db/id account)
   :security-deposit/amount-received 0
   :security-deposit/amount-required amount})

(s/fdef create
        :args (s/cat :account p/entity?
                     :amount integer?)
        :ret (s/keys :req [:db/id
                           :security-deposit/account
                           :security-deposit/amount-received
                           :security-deposit/amount-required]))


;; =============================================================================
;; Lookups
;; =============================================================================

(def by-account
  "Retrieve `security-deposit` given the owning `account`."
  (comp first :security-deposit/_account))

(s/fdef by-account
        :args (s/cat :account p/entity?)
        :ret p/entity?)

(def by-charge
  "Produce the security deposit given `charge`."
  :security-deposit/_charges)

(s/fdef by-charge
        :args (s/cat :charge p/entity?)
        :ret p/entity?)
