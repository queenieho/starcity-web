(ns starcity.datomic.schema.security-deposit.add-check-ref
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.1.1"} schema
  (s/generate-schema
   [(s/schema
     security-deposit
     (s/fields
      [checks :ref :many :component
       "Any checks that have been received to pay this security deposit."]))]))
