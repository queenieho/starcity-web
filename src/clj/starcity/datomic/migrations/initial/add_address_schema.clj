(ns starcity.datomic.migrations.initial.add-address-schema
  (:require [starcity.datomic.migrations :refer [defnorms]]))

(defnorms add-address-schema
  :txes [{:db/id                 #db/id[:db.part/db]
          :db/ident              :address/lines
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db/doc                "Address lines, separated by newlines."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :address/state
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :address/city
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :address/postal-code
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db.install/_attribute :db.part/db}])
