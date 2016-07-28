(ns migrations.pet-2016-07-26-23-22-29)

(def migration
  [{:db/id                 #db/id[:db.part/db]
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
    :db.install/_attribute :db.part/db}])

(def norms
  {:starcity/pet-2016-07-26-23-22-29 {:txes [migration]}})
