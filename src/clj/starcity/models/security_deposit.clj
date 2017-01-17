(ns starcity.models.security-deposit
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when]]
            [starcity spec
             [datomic :refer [conn]]]
            [starcity.models
             [check :as check]
             [stripe :as stripe]]
            [starcity.util :refer :all]))

;; =============================================================================
;; API
;; =============================================================================

;; TODO: Does this belong in the security-deposit ns?

(def check-statuses
  #{:check.status/deposited
    :check.status/cleared
    :check.status/cancelled
    :check.status/bounced})

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

(defn- new-amount-received
  [security-deposit check]
  (int (+ (sum-checks security-deposit)
          (sum-charges security-deposit)
          (check/amount check))))

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
(def account :security-deposit/account)

;; =============================================================================
;; Predicates

(defn is-unpaid? [security-deposit]
  (= 0 (get security-deposit :security-deposit/amount-received 0)))

(def is-paid? (comp not is-unpaid?))

(defn paid-in-full? [security-deposit]
  (and (is-paid? security-deposit)
       (= (:security-deposit/amount-received security-deposit)
          (:security-deposit/amount-required security-deposit))))

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
