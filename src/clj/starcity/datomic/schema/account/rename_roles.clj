(ns starcity.datomic.schema.account.rename-roles
  "Our original language for roles (tenant/pending) is inaccurate -- rename them
  to something more like the language we actually use (member/onboarding).")

(def ^{:added "1.1.4"} schema
  [{:db/id    :account.role/tenant
    :db/ident :account.role/member}
   {:db/id    :account.role/pending
    :db/ident :account.role/onboarding}])
