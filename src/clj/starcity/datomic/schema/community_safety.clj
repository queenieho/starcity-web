(ns starcity.datomic.schema.community-safety
  (:require [datomic-schema.schema :as s]
            [starcity.datomic.schema.community-safety
             [add-consent-given :as add-consent-given]]))

(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     community-safety
     (s/fields
      [account :ref
       "Account associated with this community safety information."]

      ;; TODO: add `:db.unique/ident`
      [report-url :string
       "API Location of the Community Safety info."]

      [wants-report? :boolean
       "Indicates whether or not this user wants a copy of their report."]))]))

(def norms
  {:starcity/add-community-safety-schema
   {:txes [schema]}

   :schema/add-community-safety-consent-9-28-16
   {:txes [add-consent-given/schema]}})
