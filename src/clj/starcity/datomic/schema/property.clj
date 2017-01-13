(ns starcity.datomic.schema.property
  (:require [datomic-schema.schema :as s]
            [starcity.datomic.schema.property
             [property-license :as property-license]
             [unit :as unit]
             [add-stripe-credentials :as add-stripe-credentials]
             [improvements :as improvements]
             [add-ops-fee :as add-ops-fee]]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     property
     (s/fields
      [name :string :fulltext]
      [description :string :fulltext]

      ;; TODO: Deprecate
      [cover-image-url :string]

      [internal-name :string :unique-identity :fulltext]
      [address :ref :component]

      [units :ref :many
       "The units that exist in this property."]
      [licenses :ref :many
       "The licenses that are available for this property."]
      [available-on :instant
       "The date that this property will come online."]

      ;; TODO: deprecate?
      ;; TODO: better doc
      [upcoming :string
       "The date that this property will come online."]))]))

(def norms
  {:starcity/add-property-schema
   {:txes [schema]}

   :starcity/add-property-license-schema
   {:txes [property-license/schema]}

   :starcity/add-unit-schema
   {:txes [unit/schema]}

   :schema/add-stripe-credentials-to-property-schema-9-8-16
   {:txes [add-stripe-credentials/schema]}

   :schema/improvements-11-20-16
   {:txes     [improvements/schema]
    :requires [:starcity/add-property-schema]}

   :schema.property/add-ops-fee-12-14-16
   {:txes [add-ops-fee/schema]}

   :schema.property.unit/unit-improvements-1-13-17
   {:txes     [unit/unit-improvements]
    :requires [:starcity/add-unit-schema]}})
