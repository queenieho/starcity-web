(ns starcity.datomic.migrations.charge-2016-07-26-23-19-11)

(def migration
  [{:db/id                 #db/id[:db.part/db]
    :db/ident              :charge/stripe-id
    :db/unique             :db.unique/identity
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db/doc                "The Stripe ID for this charge."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :charge/account
    :db/valueType          :db.type/ref
    :db/cardinality        :db.cardinality/one
    :db/doc                "The account with which this charge is associated."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :charge/purpose
    :db/valueType          :db.type/string
    :db/cardinality        :db.cardinality/one
    :db/fulltext           true
    :db/doc                "Description of the purpose of this charge."
    :db.install/_attribute :db.part/db}])

(def norms
  {:starcity/charge-2016-07-26-23-19-11 {:txes [migration]}})
