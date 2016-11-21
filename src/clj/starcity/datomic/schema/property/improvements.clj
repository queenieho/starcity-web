(ns starcity.datomic.schema.property.improvements
  (:require [datomic-schema.schema :as s]))

(def ^{:added "1.1.4"} schema
  [{:db/id               :property/units
    :db/isComponent      true
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :property/licenses
    :db/isComponent      true
    :db/index            true
    :db.alter/_attribute :db.part/db}])
