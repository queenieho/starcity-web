(ns starcity.datomic.schema.account.license-alterations
  "Change the `:account/license` attribute to have cardinality many and be a
  component.")

(def ^{:added "1.2.0"} schema
  [{:db/id               :account/license
    :db/cardinality      :db.cardinality/many
    :db/isComponent      true
    :db.alter/_attribute :db.part/db}])
