(ns starcity.datomic.schema.address.add-international-support
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.1.0"} schema
  [{:db/id    :address/city
    :db/ident :address/locality
    :db/doc   "City/town"}
   {:db/id    :address/state
    :db/ident :address/region
    :db/doc   "State/province/region."}
   {:db/id                 #db/id[:db.part/db]
    :db/ident              :address/country
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db/doc                "Country"
    :db.install/_attribute :db.part/db}])
