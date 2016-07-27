(ns starcity.datomic.migrations.address-2016-07-26-23-18-26)

(def migration
  [{:db/id                 #db/id[:db.part/db]
    :db/ident              :address/lines
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db/doc                "Address lines, separated by newlines."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :address/state
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :address/city
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :address/postal-code
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db.install/_attribute :db.part/db}])

(def norms
  {:starcity/address-2016-07-26-23-18-26 {:txes [migration]}})
