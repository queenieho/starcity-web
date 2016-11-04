(ns starcity.datomic.migrations.add-check-schema-11-4-16
  (:require [starcity.config.datomic :refer [partition]]
            [datomic.api :as d]))

(def add-check-schema
  {:schema/add-check-schema-11-4-16
   {:txes [(fn [_]
             [{:db/id                 #db/id[:db.part/db]
               :db/ident              :check/name
               :db/valueType          :db.type/string
               :db/cardinality        :db.cardinality/one
               :db/doc                "Name of person who wrote check."
               :db.install/_attribute :db.part/db}

              {:db/id                 #db/id[:db.part/db]
               :db/ident              :check/bank
               :db/valueType          :db.type/string
               :db/cardinality        :db.cardinality/one
               :db/doc                "Name of the bank that this check is associated with."
               :db.install/_attribute :db.part/db}

              {:db/id                 #db/id[:db.part/db]
               :db/ident              :check/amount
               :db/valueType          :db.type/float
               :db/cardinality        :db.cardinality/one
               :db/doc                "Amount of money that has been received for this check."
               :db.install/_attribute :db.part/db}

              {:db/id                 #db/id[:db.part/db]
               :db/ident              :check/number
               :db/valueType          :db.type/long
               :db/cardinality        :db.cardinality/one
               :db/doc                "The check number."
               :db.install/_attribute :db.part/db}

              {:db/id                 #db/id[:db.part/db]
               :db/ident              :check/date
               :db/valueType          :db.type/instant
               :db/cardinality        :db.cardinality/one
               :db/doc                "The date on the check."
               :db.install/_attribute :db.part/db}

              {:db/id                 #db/id[:db.part/db]
               :db/ident              :check/received-on
               :db/valueType          :db.type/instant
               :db/cardinality        :db.cardinality/one
               :db/doc                "Date that we received the check."
               :db.install/_attribute :db.part/db}

              {:db/id                 #db/id[:db.part/db]
               :db/ident              :check/status
               :db/valueType          :db.type/ref
               :db/cardinality        :db.cardinality/one
               :db/doc                "Status of the check wrt operations."
               :db.install/_attribute :db.part/db}

              {:db/id    (d/tempid partition)
               :db/ident :check.status/deposited}
              {:db/id    (d/tempid partition)
               :db/ident :check.status/cleared}
              {:db/id    (d/tempid partition)
               :db/ident :check.status/bounced}
              {:db/id    (d/tempid partition)
               :db/ident :check.status/cancelled}])]
    :requires [:starcity/add-starcity-partition]}})

(def add-checks-to-security-deposit-schema
  {:schema/add-checks-to-security-deposit-schema-11-4-16
   {:txes     [[{:db/id                 #db/id[:db.part/db]
                 :db/ident              :security-deposit/checks
                 :db/valueType          :db.type/ref
                 :db/isComponent        true
                 :db/cardinality        :db.cardinality/many
                 :db/doc                "Any checks that have been received to pay this security deposit."
                 :db.install/_attribute :db.part/db}]]
    :requires [:schema/add-check-schema-11-4-16
               :schema/add-security-deposit-schema-8-18-16]}})
