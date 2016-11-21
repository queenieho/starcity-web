(ns starcity.datomic.schema.security-deposit
  (:require [starcity.datomic.partition :refer [tempid]]
            [datomic-schema.schema :as s]
            [starcity.datomic.schema.security-deposit
             [add-check-ref :as add-check-ref]
             [check :as check]
             [change-charge-to-charges :as change-charge-to-charges]]))

(def ^{:added "1.1.x"} schema
  (s/generate-schema
   [(s/schema
     security-deposit
     (s/fields
      [account :ref
       "Account with which this security deposit is associated."]

      ;; TODO: change to float
      [amount-received :long
       "Amount of money that has been received for this security deposit in cents."]

      ;; TODO: change to float
      [amount-required :long
       "Amount of money that is needed for this security deposit in cents."]

      [payment-method :ref
       "Method of payment for security deposit."]

      [payment-type :ref
       "Method of payment for security deposit."]

      [due-by :instant
       "Datetime by which security deposit must be paid."]

      [charge :ref
       "Reference to the Stripe charge entity in event of ACH."]

      [check-cleared? :boolean
       "Flag for us to reflect successful clearance of check (when applicable)."]))]))

(def ^{:added "1.1.x"} payment-methods
  [{:db/id    (tempid)
    :db/ident :security-deposit.payment-method/ach}
   {:db/id    (tempid)
    :db/ident :security-deposit.payment-method/check}])

(def ^{:added "1.1.x"} payment-types
  [{:db/id    (tempid)
    :db/ident :security-deposit.payment-type/partial}
   {:db/id    (tempid)
    :db/ident :security-deposit.payment-type/full}])

(def norms
  {:schema/add-security-deposit-schema-8-18-16
   {:txes     [(concat schema payment-methods payment-types)]
    :requires [:starcity/add-starcity-partition]}

   :schema/add-checks-to-security-deposit-schema-11-4-16
   {:txes [add-check-ref/schema]}

   :schema/add-check-schema-11-4-16
   {:txes     [check/schema]
    :requires [:starcity/add-starcity-partition]}

   :schema/alter-security-deposit-schema-11-2-16
   {:txes     [change-charge-to-charges/schema]
    :requires [:schema/add-security-deposit-schema-8-18-16]}})
