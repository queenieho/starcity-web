(ns starcity.datomic.migrations.initial.add-request-status-schema
  (:require [starcity.datomic.migrations :refer [defnorms]]))

;; This is weird...not sure if it was a good idea.
(defnorms add-request-status-schema
  :txes [{:db/id                 #db/id[:db.part/db]
          :db/ident              :request-status/of
          :db/valueType          :db.type/ref
          :db/cardinality        :db.cardinality/one
          :db/doc                "Entity with which this status is associated."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :request-status/code
          :db/valueType          :db.type/long
          :db/cardinality        :db.cardinality/one
          :db/doc                "Error Code from external service."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :request-status/message
          :db/valueType          :db.type/string
          :db/cardinality        :db.cardinality/one
          :db/doc                "Any message associated with this status."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :request-status/status
          :db/valueType          :db.type/ref
          :db/cardinality        :db.cardinality/one
          :db/doc                "The status."
          :db.install/_attribute :db.part/db}

         {:db/id                 #db/id[:db.part/db]
          :db/ident              :request-status/recorded-at
          :db/valueType          :db.type/instant
          :db/cardinality        :db.cardinality/one
          :db/doc                "Time this status was recorded."
          :db.install/_attribute :db.part/db}

         {:db/id    #db/id[:db.part/starcity]
          :db/ident :request-status.status/success}
         {:db/id    #db/id[:db.part/starcity]
          :db/ident :request-status.status/failure}
         {:db/id    #db/id[:db.part/starcity]
          :db/ident :request-status.status/indeterminate}]
  :requires [add-starcity-partition])
