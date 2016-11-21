(ns starcity.datomic.schema.license
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.0.0"} license
  (s/schema
   license
   (s/fields
    [term :long
     "The term of the license in months."])))

(def schema
  (s/generate-schema [license]))

(def norms
  {:starcity/add-license-schema
   {:txes [schema]}})
