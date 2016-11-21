(ns starcity.datomic.schema.account.add-role-pending
  (:require [starcity.datomic.partition :refer [tempid]]))

(def ^{:added "1.1.x"} txes
  [{:db/id    (tempid)
    :db/ident :account.role/pending}])
