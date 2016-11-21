(ns starcity.datomic.schema.member-application.add-has-pet
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.0.x"} schema
  (s/generate-schema
   [(s/schema
     member-application
     (s/fields
      [has-pet :boolean
       "Whether or not applicant has a pet."]))]))
