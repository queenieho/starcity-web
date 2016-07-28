(ns starcity.datomic.migrations.initial.add-member-license-schema
  (:require [starcity.datomic.migrations :refer [defnorms]]))

(defnorms add-member-license-schema
  :txes [{:db/id                 #db/id[:db.part/db]
          :db/ident              :member-license/license
          :db/valueType          :db.type/ref
          :db/cardinality        :db.cardinality/one
          :db/doc                "Reference to the license that this member has agreed to."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :member-license/price
          :db/valueType          :db.type/float
          :db/cardinality        :db.cardinality/one
          :db/doc                "The price of the member's license per month. This
                          includes the base price of the license plus any additional
                          fees, e.g. for pets."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :member-license/commencement-date
          :db/valueType          :db.type/instant
          :db/cardinality        :db.cardinality/one
          :db/doc                "The date that this license takes effect."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :member-license/end-date
          :db/valueType          :db.type/instant
          :db/cardinality        :db.cardinality/one
          :db/doc                "The date that this license ends."
          :db.install/_attribute :db.part/db}])
