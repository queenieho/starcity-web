(ns starcity.datomic.schema.security-deposit.change-charge-to-charges)

(def ^{:added "< 1.1.3"} schema
  [{:db/id               :security-deposit/charge
    :db/ident            :security-deposit/charges
    :db/cardinality      :db.cardinality/many
    :db.alter/_attribute :db.part/db}])
