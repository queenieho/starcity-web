(ns starcity.datomic.migrations.add-approval-schema-9-8-16)

(def add-approval-schema
  {:schema/add-approval-schema-9-8-16
   {:txes [[{:db/id                 #db/id[:db.part/db]
             :db/ident              :approval/account
             :db/valueType          :db.type/ref
             :db/cardinality        :db.cardinality/one
             :db/doc                "The account that is being approved."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :approval/approved-by
             :db/valueType          :db.type/ref
             :db/cardinality        :db.cardinality/one
             :db/doc                "Administrator that approved this account."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :approval/approved-on
             :db/valueType          :db.type/instant
             :db/cardinality        :db.cardinality/one
             :db/doc                "Instant at which this approval was made."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :approval/property
             :db/valueType          :db.type/ref
             :db/cardinality        :db.cardinality/one
             :db/doc                "The property that this account is being approved for."
             :db.install/_attribute :db.part/db}]]}})

(def seed-test-approval
  {:seed/seed-test-approval-9-8-16
   {:txes     [[{:db/id                #db/id[:db.part/starcity]
                 :approval/account     [:account/email "onboarding@test.com"]
                 :approval/approved-by [:account/email "admin@test.com"]
                 :approval/property    [:property/internal-name "2072mission"]}]]
    :requires [:schema/add-approval-schema-9-8-16
               :starcity/seed-test-accounts
               :starcity/seed-mission]}})
