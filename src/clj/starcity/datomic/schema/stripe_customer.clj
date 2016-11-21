(ns starcity.datomic.schema.stripe-customer
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.1.x"} schema
  (s/generate-schema
   [(s/schema
     stripe-customer
     (s/fields
      [customer-id :string :unique-identity
       "The id used by Stripe to represent this customer."]
      [account :ref
       "Reference to the account with which this Stripe customer is associated."]
      [bank-account-token :string
       "The Stripe bank account token for this customer's bank account."]))]))

(def norms
  {:schema/add-stripe-customer-schema-8-30-16
   {:txes [schema]}})
