(ns starcity.datomic.schema.property.property-license
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     property-license
     (s/fields
      [license :ref
       "Reference to a license for a specific property."]
      [base-price :float
       "The base price for this license at this property."]))]))
