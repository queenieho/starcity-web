(ns starcity.datomic.schema.member-application
  (:require [datomic-schema.schema :as s]
            [starcity.datomic.schema.member-application
             [community-fitness :as community-fitness]
             [pet :as pet]
             [add-has-pet :as add-has-pet]
             [add-status :as add-status]
             [improvements :as improvements]]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     member-application
     (s/fields
      [desired-properties :ref :many
       "Properties that applicant is interested in."]

      [desired-license :ref :component
       "License that this applicant wants."]

      [desired-availability :instant
       "Date that applicant would like to move in."]

      [pet :ref :component]

      [community-fitness :ref :component
       "The community fitness questionnaire."]

      [current-address :ref :component
       "Applicant's current address."]

      [locked :boolean
       "Indicates whether or not the application is locked for edits."]

      [approved :boolean
       "Indicates whether or not the application has been approved or not by an administrator."]

      [submitted-at :instant
       "The time at which the application was submitted."]))]))

(def norms
  {:starcity/add-member-application-schema
   {:txes [schema]}

   :starcity/add-community-fitness-schema
   {:txes [community-fitness/schema]}

   :starcity/add-pet-schema
   {:txes [pet/schema]}

   :schema/add-has-pet-attr-10-3-16
   {:txes [add-has-pet/schema]}

   :schema/add-member-application-status-11-15-16
   {:txes     [add-status/schema]
    :requires [:starcity/add-starcity-partition]}

   :schema.account/improvements-11-20-16
   {:txes     [improvements/schema]
    :requires [:starcity/add-member-application-schema
               :schema/add-has-pet-attr-10-3-16
               :schema/add-member-application-status-11-15-16]}})
