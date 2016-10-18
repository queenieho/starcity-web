(ns starcity.datomic.migrations.add-community-safety-consent-9-28-16)

(def add-community-safety-consent
  {:schema/add-community-safety-consent-9-28-16
   {:txes [[{:db/id                 #db/id[:db.part/db]
             :db/ident              :community-safety/consent-given?
             :db/valueType          :db.type/boolean
             :db/cardinality        :db.cardinality/one
             :db/doc                "Has user given us consent to perform a background check?"
             :db.install/_attribute :db.part/db}]]}})
