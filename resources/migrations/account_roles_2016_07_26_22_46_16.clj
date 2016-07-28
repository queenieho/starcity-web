(ns migrations.account-roles-2016-07-26-22-46-16)

(def migration
  [{:db/id    #db/id[:db.part/starcity]
    :db/ident :account.role/applicant}
   {:db/id    #db/id[:db.part/starcity]
    :db/ident :account.role/tenant}
   {:db/id    #db/id[:db.part/starcity]
    :db/ident :account.role/admin}])

(def norms
  {:starcity/account-roles-2016-07-26-22-46-16 {:txes     [migration]
                                                :requires [:starcity/starcity-partition]}})
