(ns starcity.datomic.migrations.member-application-2016-07-26-23-21-47)

(def migration
  [{:db/id                 #db/id[:db.part/db]
    :db/ident              :member-application/current-address
    :db/valueType          :db.type/ref
    :db/cardinality        :db.cardinality/one
    :db/isComponent        true
    :db/doc                "Applicant's current address."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :member-application/desired-properties
    :db/valueType          :db.type/ref
    :db/cardinality        :db.cardinality/many
    :db/doc                "Desired properties."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :member-application/desired-license
    :db/valueType          :db.type/ref
    :db/cardinality        :db.cardinality/one
    :db/isComponent        true
    :db/doc                "Desired license (reference to license)."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :member-application/desired-availability
    :db/valueType          :db.type/instant
    :db/cardinality        :db.cardinality/one
    :db/doc                "Desired availability."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :member-application/pet
    :db/valueType          :db.type/ref
    :db/cardinality        :db.cardinality/one
    :db/isComponent        true
    :db/doc                "Applicant's pet."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :member-application/community-fitness
    :db/valueType          :db.type/ref
    :db/cardinality        :db.cardinality/one
    :db/isComponent        true
    :db/doc                "Reference to the community fitness questionnaire."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :member-application/locked
    :db/valueType          :db.type/boolean
    :db/cardinality        :db.cardinality/one
    :db/doc                "Indicates whether or not the application is locked for edits."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :member-application/submitted-at
    :db/valueType          :db.type/instant
    :db/cardinality        :db.cardinality/one
    :db/doc                "The time at which the application was submitted."
    :db.install/_attribute :db.part/db}

   {:db/id                 #db/id[:db.part/db]
    :db/ident              :member-application/approved
    :db/valueType          :db.type/boolean
    :db/cardinality        :db.cardinality/one
    :db/doc                "Indicates whether or not the application has been approved or not by an administrator."
    :db.install/_attribute :db.part/db}])

(def norms
  {:starcity/member-application-2016-07-26-23-21-47 {:txes [migration]}})
