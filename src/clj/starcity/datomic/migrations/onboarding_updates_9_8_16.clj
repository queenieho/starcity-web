(ns starcity.datomic.migrations.onboarding-updates-9-8-16)

(def add-stripe-credentials-to-property-schema
  {:schema/add-stripe-credentials-to-property-schema-9-8-16
   {:txes [[{:db/id                 #db/id[:db.part/db]
             :db/ident              :property/managed-account-id
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/doc                "The id of the managed Stripe account associated with this property."
             :db.install/_attribute :db.part/db}]]}})

(def seed-stripe-test-credentials
  {:seed/seed-stripe-test-credentials-9-8-16
   {:txes     [[{:db/id                      [:property/internal-name "2072mission"]
                 :property/managed-account-id "acct_191838JDow24Tc1a"}
                {:db/id                      [:property/internal-name "52gilbert"]
                 :property/managed-account-id "acct_191838JDow24Tc1a"}]]
    :requires [:schema/add-stripe-credentials-to-property-schema-9-8-16
               :starcity/seed-mission
               :starcity/seed-gilbert]}})
