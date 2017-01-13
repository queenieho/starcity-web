(ns starcity.datomic.schema.account
  (:require [starcity.datomic.partition :refer [tempid]]
            [datomic-schema.schema :as s]
            [starcity.datomic.schema.account
             [add-role-pending :as add-role-pending]
             [add-indexes :as add-indexes]
             [rename-roles :as rename-roles]
             [license-alterations :as license-alterations]]))


(def ^{:added "1.0.0"} schema
  (s/generate-schema
   [(s/schema
     account
     (s/fields
      [first-name :string :fulltext]
      [middle-name :string :fulltext]
      [last-name :string :fulltext]
      [phone-number :string]
      [email :string :unique-identity :fulltext]

      [password :string
       "User's hashed password."]

      [member-application :ref
       "The rental application associated with this account."]

      [unit :ref
       "The unit that the person identified by this account is living in."]

      [license :ref
       "The user's license."]

      [activation-hash :string
       "The user's activation hash, generated at the time of signup."]

      [activated :boolean
       "Becomes true after account activation."]

      [dob :instant
       "User's date of birth."]

      [role :enum]))]))

(def ^{:added "1.0.0"} roles
  [{:db/id    (tempid)
    :db/ident :account.role/applicant}
   {:db/id    (tempid)
    :db/ident :account.role/tenant}
   {:db/id    (tempid)
    :db/ident :account.role/admin}])

(def norms
  {:starcity/add-account-schema
   {:txes [schema]}

   :starcity/add-account-roles
   {:txes     [roles]
    :requires [:starcity/add-starcity-partition]}

   :seed/add-account-role-pending-8-18-16
   {:txes     [add-role-pending/txes]
    :requires [:starcity/add-starcity-partition]}

   :schema.account/add-indexes-11-20-16
   {:txes     [add-indexes/schema]
    :requires [:starcity/add-account-schema]}

   :schema.account/rename-roles
   {:txes     [rename-roles/schema]
    :requires [:starcity/add-account-roles]}

   :schema.account/license-alterations
   {:txes     [license-alterations/schema]
    :requires [:starcity/add-account-schema]}})
