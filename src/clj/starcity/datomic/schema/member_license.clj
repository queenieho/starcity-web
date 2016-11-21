(ns starcity.datomic.schema.member-license
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.0.0"} member-license
  (s/schema
   member-license
   (s/fields
    [license :ref
     "Reference to the license that this member has agreed to."]

    [price :float
     "The price of the member's license per month. This includes the base price
     of the license plus any additional fees, e.g. for pets."]

    ;; TODO: rename to `commencement`
    [commencement-date :instant
     "The date that this license takes effect."]

    ;; TODO: doc?
    [end-date :instant
     "The date that this license ends."])))

(def schema
  (s/generate-schema [member-license]))

(def norms
  {:starcity/add-member-license-schema
   {:txes [schema]}})
