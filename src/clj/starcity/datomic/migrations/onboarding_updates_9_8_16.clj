(ns starcity.datomic.migrations.onboarding-updates-9-8-16)

(def add-stripe-credentials-to-property-schema
  {:schema/add-stripe-credentials-to-property-schema-9-8-16
   {:txes [[{:db/id                 #db/id[:db.part/db]
             :db/ident              :property/stripe-secret-key
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/doc                "The secret key for the Stripe account associated with this property."
             :db.install/_attribute :db.part/db}
            {:db/id                 #db/id[:db.part/db]
             :db/ident              :property/stripe-public-key
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/doc                "The public key for the Stripe account associated with this property."
             :db.install/_attribute :db.part/db}]]
    :requires [:starcity/add-property-schema]}})

(def seed-stripe-test-credentials
  {:seed/seed-stripe-test-credentials-9-8-16
   {:txes     [[{:db/id                      [:property/internal-name "2072mission"]
                 :property/stripe-secret-key "sk_test_iwTWDj4CbhFPWFgaGxHLMAag"
                 :property/stripe-public-key "pk_test_F91qZqWHIBxJEJGNzbhpKzYX"}]]
    :requires [:schema/add-stripe-credentials-to-property-schema-9-8-16
               :starcity/seed-mission]}})
