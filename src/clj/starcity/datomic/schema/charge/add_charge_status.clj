(ns starcity.datomic.schema.charge.add-charge-status
  (:require [datomic-schema.schema :as s]
            [starcity.datomic.partition :refer [tempid]]))

(def statuses
  [{:db/id    (tempid)
    :db/ident :charge.status/pending}
   {:db/id    (tempid)
    :db/ident :charge.status/succeeded}
   {:db/id    (tempid)
    :db/ident :charge.status/failed}])

(def ^{:added "1.1.0"} schema
  (->> (s/generate-schema
        [(s/schema
          charge
          (s/fields
           [status :ref "The status of this charge."]))])
       (concat statuses)))
