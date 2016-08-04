(ns starcity.datomic.migrations.income-files-8-3-16)

(def add-income-files-schema
  {:starcity/add-income-files-schema-8-3-16
   {:txes [[{:db/id                 #db/id[:db.part/db]
             :db/ident              :income-file/account
             :db/valueType          :db.type/ref
             :db/cardinality        :db.cardinality/one
             :db/doc                "The account that this income file belongs to."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :income-file/content-type
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/doc                "The type of content that this file holds."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :income-file/size
             :db/valueType          :db.type/long
             :db/cardinality        :db.cardinality/one
             :db/doc                "The size of this file in bytes."
             :db.install/_attribute :db.part/db}

            {:db/id                 #db/id[:db.part/db]
             :db/ident              :income-file/path
             :db/valueType          :db.type/string
             :db/cardinality        :db.cardinality/one
             :db/doc                "The path to this file on the filesystem."
             :db.install/_attribute :db.part/db}]]}})
