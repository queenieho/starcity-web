(ns starcity.datomic.schema.member-license.rent-payment
  (:require [starcity.datomic.partition :refer [tempid]]
            [datomic-schema.schema :as s])
  (:refer-clojure :exclude [methods]))

(def methods
  [{:db/id    (tempid)
    :db/ident :rent-payment.method/check}
   {:db/id    (tempid)
    :db/ident :rent-payment.method/autopay}
   {:db/id    (tempid)
    :db/ident :rent-payment.method/ach}
   {:db/id    (tempid)
    :db/ident :rent-payment.method/other}])

(def statuses
  [{:db/id    (tempid)
    :db/ident :rent-payment.status/due}
   {:db/id    (tempid)
    :db/ident :rent-payment.status/pending}
   {:db/id    (tempid)
    :db/ident :rent-payment.status/paid}])

(def ^{:added "1.2.0"} schema
  (-> (s/generate-schema
       [(s/schema
         rent-payment
         (s/fields
          [method :ref :index
           "The method of payment."]

          [status :ref :index
           "The status of this payment."]

          [amount :float :index
           "The amount in dollars that was paid."]

          [period-start :instant :index
           "The start date of this payment period."]

          [period-end :instant :index
           "The end date of this payment period."]

          [due-date :instant :index
           "The due date for this payment."]

          [paid-on :instant :index
           "Date that this payment was successfully paid on."]

          [check :ref :component
           "The associated check entity, in the event that `method` is `check`."]

          [charge :ref :component
           "The associated charge entity, in the event that `method` is `ach`."]

          [method-desc :string :fulltext
           "Description of the payment method, in the event that `method` is `other`."]

          [notes :ref :fulltext :many :component
           "Reference to any notes that have been added to this payment."]

          [invoice-id :string :unique-identity
           "The id of the Stripe Invoice if this payment is made with autopay."]

          [autopay-failures :long :index
           "The number of times that this payment has failed through autopay."]))])
      (concat methods statuses)
      (vec)))
