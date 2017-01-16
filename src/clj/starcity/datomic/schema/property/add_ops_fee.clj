(ns starcity.datomic.schema.property.add-ops-fee
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.2.0"} schema
  (s/generate-schema
   [(s/schema
     property
     (s/fields
      [ops-fee :float :index
       "The percentage fee that Starcity Ops takes from payments to this property."]))]))
