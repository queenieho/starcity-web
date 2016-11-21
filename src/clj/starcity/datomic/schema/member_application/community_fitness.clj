(ns starcity.datomic.schema.member-application.community-fitness
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     community-fitness
     (s/fields
      ;; TODO: rename to `experience`
      [prior-community-housing :string :fulltext
       "Response to: 'Have you ever lived in community housing?'"]

      [skills :string :fulltext
       "Response to: 'What skills or traits do you hope to share with the community?'"]

      [why-interested :string :fulltext
       "Response to: 'Why are you interested in Starcity?'"]

      [free-time :string :fulltext
       "Response to: 'How do you spend your free time'"]

      [dealbreakers :string :fulltext
       "Response to: 'Do you have an dealbreakers?'"]))]))
