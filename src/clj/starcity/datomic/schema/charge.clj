(ns starcity.datomic.schema.charge
  (:require [datomic-schema.schema :as s]
            [starcity.datomic.schema.charge
             [add-charge-status :as add-charge-status]]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     charge
     (s/fields
      [stripe-id :string :unique-identity
       "The Stripe ID for this charge."]

      [account :ref
       "The account with which this charge is associated."]

      [purpose :string :fulltext
       "Description of the purpose of this charge."]))]))

(def norms
  {:starcity/add-charge-schema
   {:txes [schema]}

   :schema/add-charge-status-11-1-16
   {:txes     [add-charge-status/schema]
    :requires [:starcity/add-starcity-partition]}})
