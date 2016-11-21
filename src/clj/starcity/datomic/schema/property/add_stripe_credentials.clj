(ns starcity.datomic.schema.property.add-stripe-credentials
  (:require [datomic-schema.schema :as s]))

(def ^{:added "< 1.1.3"} schema
  (s/generate-schema
   [(s/schema
     property
     (s/fields
      ;; TODO: :unique-identity
      [managed-account-id :string
       "The id of the managed Stripe account associated with this property."]))]))
