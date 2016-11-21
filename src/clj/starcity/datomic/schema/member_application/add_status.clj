(ns starcity.datomic.schema.member-application.add-status
  (:require [datomic-schema.schema :as s]
            [starcity.datomic.partition :refer [tempid]]))

(def ^:private statuses
  [{:db/id    (tempid)
    :db/ident :member-application.status/in-progress}
   {:db/id    (tempid)
    :db/ident :member-application.status/submitted}
   {:db/id    (tempid)
    :db/ident :member-application.status/approved}
   {:db/id    (tempid)
    :db/ident :member-application.status/rejected}])

(def ^{:added "1.1.3"} schema
  (->> (s/generate-schema
        [(s/schema
          member-application
          (s/fields
           [status :ref
            "The status of this member's application."]))])
       (concat statuses)))
