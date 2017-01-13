(ns starcity.datomic.schema.account.add-indexes
  "Add indexes to attribues that do not have them.")

(def ^{:added "1.1.4"} schema
  [{:db/id               :account/first-name
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/middle-name
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/last-name
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/phone-number
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/member-application
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/unit
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/license
    :db/index            true
    :db.alter/_attribute :db.part/db}
   {:db/id               :account/activation-hash
    :db/index            true
    :db.alter/_attribute :db.part/db}])
