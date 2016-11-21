(ns starcity.datomic.schema.address
  (:require [datomic-schema.schema :as s]
            [starcity.datomic.schema.address
             [add-international-support :as add-international-support]]))

(def ^{:added "1.0.0"} address
  (s/schema
   address
   (s/fields
    [lines :string "Address lines, separated by newlines."]
    [state :string]
    [city :string]
    [postal-code :string])))

(def schema
  (s/generate-schema [address]))

(def norms
  {:starcity/add-address-schema
   {:txes [schema]}

   :schema/alter-address-schema-10-8-16
   {:txes     [add-international-support/schema]
    :requires [:starcity/add-address-schema]}})
