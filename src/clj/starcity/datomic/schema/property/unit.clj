(ns starcity.datomic.schema.property.unit
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     unit
     (s/fields
      [name :string :fulltext
       "Name of the unit."]
      [description :string :fulltext
       "Description of the unit."]
      [price :float
       "Additional per-month price of this unit on top of monthly lease."]
      [available-on :instant
       "The date that this unit is available for lease."]
      [floor :long
       "The floor that this unit is on."]
      [dimensions :ref :component
       "The dimensions of this unit."]))

    (s/schema
     unit-dimension
     (s/fields
      [height :float
       "Height of unit in feet."]
      [width :float
       "Width of unit in feet."]
      [length :float
       "Length/depth of unit in feet."]))]))
