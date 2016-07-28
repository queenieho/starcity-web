(ns starcity.datomic.migrations.initial.add-property-schema
  (:require [starcity.datomic.migrations :refer [defnorms]]))

(defnorms add-property-schema
  :txes [{:db/id                 #db/id[:db.part/db]
          :db/ident              :property/name
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db/fulltext           true
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :property/description
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db/fulltext           true
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :property/cover-image-url
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :property/internal-name
          :db/unique             :db.unique/identity
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db/fulltext           true
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :property/address
          :db/valueType          :db.type/ref
          :db/cardinality        :db.cardinality/one
          :db/isComponent        true
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :property/units
          :db/valueType          :db.type/ref
          :db/cardinality        :db.cardinality/many
          :db/doc                "The units that exist in this property."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :property/licenses
          :db/valueType          :db.type/ref
          :db/cardinality        :db.cardinality/many
          :db/isComponent        true
          :db/doc                "The licenses that are available for this property."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :property/available-on
          :db/valueType          :db.type/instant
          :db/cardinality        :db.cardinality/one
          :db/doc                "The date that this property will come online."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :property/upcoming
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db/doc                "The date that this property will come online."
          :db.install/_attribute :db.part/db}])
