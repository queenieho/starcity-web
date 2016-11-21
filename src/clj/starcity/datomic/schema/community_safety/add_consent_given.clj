(ns starcity.datomic.schema.community-safety.add-consent-given
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.0.x"} schema
  (s/generate-schema
   [(s/schema
     community-safety
     (s/fields
      [consent-given? :boolean
       "Has user given us consent to perform a background check?"]))]))
