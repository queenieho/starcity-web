(ns starcity.datomic.migrations.initial.add-starcity-partition
  (:require [starcity.datomic.migrations :refer [defnorms]]))

(defnorms add-starcity-partition
  :txes [{:db/id                 #db/id[:db.part/db]
          :db/ident              :db.part/starcity
          :db.install/_partition :db.part/db}])
