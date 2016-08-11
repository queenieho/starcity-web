(ns starcity.datomic.migrations.initial.add-community-fitness-schema)

(def add-community-fitness-schema
  {:starcity/add-community-fitness-schema
   {:txes [[{:db/id                 #db/id[:db.part/db]
             :db/ident              :community-fitness/prior-community-housing
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/fulltext           true
             :db/doc                "Response to: 'Have you ever lived in community housing?'"
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :community-fitness/skills
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/fulltext           true
             :db/doc                "Response to: 'What skills or traits do you hope to share with the community?'"
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :community-fitness/why-interested
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/fulltext           true
             :db/doc                "Response to: 'Why are you interested in Starcity?'"
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :community-fitness/free-time
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/fulltext           true
             :db/doc                "Response to: 'How do you spend your free time'"
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :community-fitness/dealbreakers
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/fulltext           true
             :db/doc                "Response to: 'Do you have an dealbreakers?'"
             :db.install/_attribute :db.part/db}]]}})
