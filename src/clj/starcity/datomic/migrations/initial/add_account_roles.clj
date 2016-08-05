(ns starcity.datomic.migrations.initial.add-account-roles)

(def add-account-roles
  {:starcity/add-account-roles
   {:txes [[{:db/id    #db/id[:db.part/starcity]
             :db/ident :account.role/applicant}
            {:db/id    #db/id[:db.part/starcity]
             :db/ident :account.role/tenant}
            {:db/id    #db/id[:db.part/starcity]
             :db/ident :account.role/admin}]]
    :requires [:starcity/add-starcity-partition]}})
