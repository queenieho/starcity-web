(ns starcity.datomic.migrations.add-stripe-customer-schema-8-30-16)

(def add-stripe-customer-schema
  {:schema/add-stripe-customer-schema-8-30-16
   {:txes [[{:db/id                 #db/id[:db.part/db]
             :db/ident              :stripe-customer/customer-id
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/unique             :db.unique/identity
             :db/doc                "The id used by Stripe to represent this customer."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :stripe-customer/account
             :db/valueType          :db.type/ref
             :db/cardinality        :db.cardinality/one
             :db/doc                "Reference to the account with which this Stripe customer is associated."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :stripe-customer/bank-account-token
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/doc                "The Stripe bank account token for this customer's bank account."
             :db.install/_attribute :db.part/db}]]}})
