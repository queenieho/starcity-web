(ns starcity.datomic.migrations.community-fitness-2016-07-26-23-22-52)

(def migration
  [{:db/id                 #db/id[:db.part/db]
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
    :db.install/_attribute :db.part/db}])

(def norms
  {:starcity/community-fitness-2016-07-26-23-22-52 {:txes [migration]}})
