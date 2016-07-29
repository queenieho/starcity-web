(ns starcity.datomic.migrations.initial.add-account-schema
  (:require [starcity.datomic.migrations :refer [defnorms]]))

(defnorms add-account-schema
  :txes [{:db/id                 #db/id[:db.part/db]
          :db/ident              :account/first-name
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db/fulltext           true
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :account/middle-name
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db/fulltext           true
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :account/last-name
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db/fulltext           true
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :account/email
          :db/unique             :db.unique/identity
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db/fulltext           true
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :account/password
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db/doc                "The user's hashed password."
          :db.install/_attribute :db.part/db}

         ;; TODO: Flip
         {:db/id                 #db/id[:db.part/db]
          :db/ident              :account/member-application
          :db/valueType          :db.type/ref
          :db/cardinality        :db.cardinality/one
          :db/doc                "The rental application associated with this account."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :account/unit
          :db/valueType          :db.type/ref
          :db/cardinality        :db.cardinality/one
          :db/doc                "The unit that the person identified by this account is living in."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :account/license
          :db/valueType          :db.type/ref
          :db/cardinality        :db.cardinality/one
          :db/doc                "The user's license."
          :db.install/_attribute :db.part/db}

         ;; TODO: Flip
         {:db/id                 #db/id[:db.part/db]
          :db/ident              :account/activation-hash
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db/doc                "The user's activation hash, generated at the time of signup."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :account/activated
          :db/valueType          :db.type/boolean
          :db/cardinality        :db.cardinality/one
          :db/doc                "Is the user's account activated?"
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :account/dob
          :db/valueType          :db.type/instant
          :db/cardinality        :db.cardinality/one
          :db/doc                "The user's date of birth."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :account/role
          :db/valueType          :db.type/ref
          :db/cardinality        :db.cardinality/one
          :db/doc                "User's roles"
          :db.install/_attribute :db.part/db}])
