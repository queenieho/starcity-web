(ns starcity.datomic.migrations.initial.add-pet-schema)

(def add-pet-schema
  {:starcity/add-pet-schema
   {:txes [[{:db/id                 #db/id[:db.part/db]
             :db/ident              :pet/type
             :db/valueType          :db.type/keyword
             :db/cardinality        :db.cardinality/one
             :db/doc                "The type of pet."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :pet/breed
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/doc                "The pet's breed."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :pet/weight
             :db/valueType          :db.type/long
             :db/cardinality        :db.cardinality/one
             :db/doc                "The weight of the pet."
             :db.install/_attribute :db.part/db}]]}})
