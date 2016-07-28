(ns starcity.datomic.migrations.initial.add-account-roles
  (:require [starcity.datomic.migrations :refer [defnorms]]))

(defnorms add-account-roles
  :txes [{:db/id    #db/id[:db.part/starcity]
          :db/ident :account.role/applicant}
         {:db/id    #db/id[:db.part/starcity]
          :db/ident :account.role/tenant}
         {:db/id    #db/id[:db.part/starcity]
          :db/ident :account.role/admin}]
  :requires [add-starcity-partition])
