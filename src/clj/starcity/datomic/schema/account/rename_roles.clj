(ns starcity.datomic.schema.account.rename-roles)

(def ^{:added "1.1.4"} schema
  [{:db/id    :account.role/tenant
    :db/ident :account.role/member}
   {:db/id    :account.role/pending
    :db/ident :account.role/onboarding}])
