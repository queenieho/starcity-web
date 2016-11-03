(ns starcity.datomic.migrations.add-charge-status-11-2-16
  (:require [starcity.config.datomic :refer [partition]]
            [datomic.api :as d]))

(def add-charge-status
  {:schema/add-charge-status-11-1-16
   {:txes [(fn [_]
             [{:db/id                 #db/id[:db.part/db]
               :db/ident              :charge/status
               :db/valueType          :db.type/ref
               :db/cardinality        :db.cardinality/one
               :db/doc                "The status of this charge."
               :db.install/_attribute :db.part/db}

              {:db/id    (d/tempid partition)
               :db/ident :charge.status/pending}
              {:db/id    (d/tempid partition)
               :db/ident :charge.status/succeeded}
              {:db/id    (d/tempid partition)
               :db/ident :charge.status/failed}])]
    :requires [:starcity/add-starcity-partition]}})
