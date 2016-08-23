(ns starcity.datomic.migrations.add-security-deposit-schema-8-18-16)

(def add-security-deposit-schema
  {:schema/add-security-deposit-schema-8-18-16
   {:txes [[{:db/id                 #db/id[:db.part/db]
             :db/ident              :security-deposit/account
             :db/valueType          :db.type/ref
             :db/cardinality        :db.cardinality/one
             :db/doc                "Account with which this security deposit is associated."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :security-deposit/amount-received
             :db/valueType          :db.type/float
             :db/cardinality        :db.cardinality/one
             :db/doc                "Amount of money that has been received for this security deposit in USD."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :security-deposit/amount-required
             :db/valueType          :db.type/float
             :db/cardinality        :db.cardinality/one
             :db/doc                "Amount of money that is needed for this security deposit."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :security-deposit/payment-method
             :db/valueType          :db.type/ref
             :db/cardinality        :db.cardinality/one
             :db/doc                "Method of payment for security deposit."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :security-deposit/payment-due-by
             :db/valueType          :db.type/instant
             :db/cardinality        :db.cardinality/one
             :db/doc                "Datetime by which security deposit must be paid."
             :db.install/_attribute :db.part/db}

            {:db/id    #db/id[:db.part/starcity]
             :db/ident :security-deposit.payment-method/ach}
            {:db/id    #db/id[:db.part/starcity]
             :db/ident :security-deposit.payment-method/check}]]

    :requires [:starcity/add-starcity-partition]}})
