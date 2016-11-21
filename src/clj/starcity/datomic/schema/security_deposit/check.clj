(ns starcity.datomic.schema.security-deposit.check
  (:require [datomic-schema.schema :as s]
            [starcity.datomic.partition :refer [tempid]]))

(def statuses
  [{:db/id    (tempid)
    :db/ident :check.status/deposited}
   {:db/id    (tempid)
    :db/ident :check.status/cleared}
   {:db/id    (tempid)
    :db/ident :check.status/bounced}
   {:db/id    (tempid)
    :db/ident :check.status/cancelled}])

(def ^{:added "1.1.1"} schema
  (->> (s/generate-schema
        [(s/schema
          check
          (s/fields
           [name :string
            "Name of person who wrote check."]
           [bank :string
            "Name of the bank that this check is associated with."]
           [amount :float
            "Amount of money that has been received for this check."]
           [number :long
            "The check number."]
           [date :instant
            "The date on the check."]
           [received-on :instant
            "Date that we received the check."]
           [status :ref
            "Status of the check wrt operations."]))])
       (concat statuses)))
