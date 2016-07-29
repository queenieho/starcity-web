(ns starcity.datomic.migrations.initial.add-community-safety-schema
  (:require [starcity.datomic.migrations :refer [defnorms]]))

(defnorms add-community-safety-schema
  :txes [{:db/id                 #db/id[:db.part/db]
          :db/ident              :community-safety/account
          :db/valueType          :db.type/ref
          :db/cardinality        :db.cardinality/one
          :db/doc                "Account associated with this community safety information."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :community-safety/report-url
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db/doc                "API Location of the Community Safety info."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :community-safety/wants-report?
          :db/valueType          :db.type/boolean
          :db/cardinality        :db.cardinality/one
          :db/doc                "Indicates whether or not this user wants a copy of their report."
          :db.install/_attribute :db.part/db}])
