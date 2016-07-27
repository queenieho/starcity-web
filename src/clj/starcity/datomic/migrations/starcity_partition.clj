(ns starcity.datomic.migrations.starcity-partition)

(def migration
  [{:db/id                 #db/id[:db.part/db]
    :db/ident              :db.part/starcity
    :db.install/_partition :db.part/db}])

(def norms
  {:starcity/starcity-partition {:txes [migration]}})
