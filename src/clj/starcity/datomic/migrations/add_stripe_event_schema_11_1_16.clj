(ns starcity.datomic.migrations.add-stripe-event-schema-11-1-16
  (:require [starcity.config.datomic :refer [partition]]
            [datomic.api :as d]))

(def add-stripe-event-schema
  {:schema/add-stripe-event-schema-11-1-16
   {:txes [(fn [_]
             [{:db/id                 #db/id[:db.part/db]
               :db/ident              :stripe-event/event-id
               :db/unique             :db.unique/identity
               :db/valueType          :db.type/string
               :db/cardinality        :db.cardinality/one
               :db/doc                "The Stripe ID for this event."
               :db.install/_attribute :db.part/db}

              {:db/id                 #db/id[:db.part/db]
               :db/ident              :stripe-event/type
               :db/valueType          :db.type/string
               :db/cardinality        :db.cardinality/one
               :db/doc                "The event's type."
               :db.install/_attribute :db.part/db}

              {:db/id                 #db/id[:db.part/db]
               :db/ident              :stripe-event/status
               :db/valueType          :db.type/ref
               :db/cardinality        :db.cardinality/one
               :db/doc                "The status of this event. One of #{:processing :succeeded :failed}"
               :db.install/_attribute :db.part/db}

              {:db/id    (d/tempid partition)
               :db/ident :stripe-event.status/processing}
              {:db/id    (d/tempid partition)
               :db/ident :stripe-event.status/succeeded}
              {:db/id    (d/tempid partition)
               :db/ident :stripe-event.status/failed}])]
    :requires [:starcity/add-starcity-partition]}})
