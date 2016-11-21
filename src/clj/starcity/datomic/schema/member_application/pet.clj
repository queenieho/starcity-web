(ns starcity.datomic.schema.member-application.pet
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.0.0"} pet
  (s/schema
   pet
   (s/fields
    ;; TODO: enum?
    [type :keyword "The type of pet."]
    [breed :string "The pet's breed."]
    [weight :long "The weight of the pet."])))

(def schema
  (s/generate-schema [pet]))
