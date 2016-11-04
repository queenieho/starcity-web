(ns starcity.datomic.migrations.alter-security-deposit-schema-11-2-16
  "Changes `:security-deposit/charge` to `:security-deposit/charges`, changing
  the cardinality from one to many.")

(def alter-security-deposit-schema
  {:schema/alter-security-deposit-schema-11-2-16
   {:txes     [[{:db/id               :security-deposit/charge
                 :db/ident            :security-deposit/charges
                 :db/cardinality      :db.cardinality/many
                 :db.alter/_attribute :db.part/db}]]
    :requires [:schema/add-security-deposit-schema-8-18-16]}})
