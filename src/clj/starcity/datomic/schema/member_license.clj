(ns starcity.datomic.schema.member-license
  (:require [datomic-schema.schema :as s]
            [starcity.datomic.schema.member-license
             [rent-alterations :as rent-alterations]
             [rent-payment :as rent-payment]]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     member-license
     (s/fields
      [license :ref
       "Reference to the license that this member has agreed to."]

      [price :float
       "The price of the member's license per month. This includes the base price
     of the license plus any additional fees, e.g. for pets."]

      [commencement-date :instant
       "The date that this license takes effect."]

      [end-date :instant
       "The date that this license ends."]))]))

(def ^{:added "1.2.0"} add-rent-payments
  (s/generate-schema
   [(s/schema
     member-license
     (s/fields
      [rent-payments :ref :many :index :component
       "References the rent payments that have been made for this license by owner."]))]))

(def norms
  {:starcity/add-member-license-schema
   {:txes [schema]}

   :schema.member-license/rent-alterations
   {:txes     [rent-alterations/schema]
    :requires [:starcity/add-member-license-schema]}

   :schema.member-license/add-rent-payments
   {:txes [rent-payment/schema
           add-rent-payments]}})
