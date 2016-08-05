(ns starcity.datomic.migrations.initial.add-license-schema)

(def add-license-schema
  {:starcity/add-license-schema
   {:txes [[{:db/id                 #db/id[:db.part/db]
             :db/ident              :license/term
             :db/valueType          :db.type/long
             :db/cardinality        :db.cardinality/one
             :db/doc                "The term of the license in months."
             :db.install/_attribute :db.part/db}]]}})
