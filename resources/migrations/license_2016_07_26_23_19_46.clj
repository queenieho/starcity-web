(ns migrations.license-2016-07-26-23-19-46)

(def migration
  [{:db/id                 #db/id[:db.part/db]
    :db/ident              :license/term
    :db/valueType          :db.type/long
    :db/cardinality        :db.cardinality/one
    :db/doc                "The term of the license in months."
    :db.install/_attribute :db.part/db}])

(def norms
  {:starcity/license-2016-07-26-23-19-46 {:txes [migration]}})
