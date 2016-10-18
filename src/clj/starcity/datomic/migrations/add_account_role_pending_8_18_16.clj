(ns starcity.datomic.migrations.add-account-role-pending-8-18-16)

(def add-account-role-pending
  {:seed/add-account-role-pending-8-18-16
   {:txes     [[{:db/id    #db/id[:db.part/starcity]
                 :db/ident :account.role/pending}]]
    :requires [:starcity/add-starcity-partition]}})
