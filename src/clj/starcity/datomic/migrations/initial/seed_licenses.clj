(ns starcity.datomic.migrations.initial.seed-licenses
  (:require [starcity.datomic.migrations :refer [defnorms]]
            [datomic.api :as d]))

(defnorms seed-licenses
  :txes [{:db/id        (d/tempid :db.part/starcity)
          :license/term 1}
         {:db/id        (d/tempid :db.part/starcity)
          :license/term 6}
         {:db/id        (d/tempid :db.part/starcity)
          :license/term 12}]
  :requires [add-starcity-partition])
