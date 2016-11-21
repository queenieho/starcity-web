(ns starcity.datomic.schema.partition
  (:require [starcity.datomic.partition :refer [part]]))

(def ^{:added "1.0.0"} partitions
  [{:db/id                 #db/id[:db.part/db]
    :db/ident              part
    :db.install/_partition :db.part/db}])

(def norms
  {:starcity/add-starcity-partition
   {:txes [partitions]}})
