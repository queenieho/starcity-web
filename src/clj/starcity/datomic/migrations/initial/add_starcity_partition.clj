(ns starcity.datomic.migrations.initial.add-starcity-partition)

(def add-starcity-partition
  {:starcity/add-starcity-partition
   {:txes [[{:db/id                 #db/id[:db.part/db]
             :db/ident              :db.part/starcity
             :db.install/_partition :db.part/db}]]}})
