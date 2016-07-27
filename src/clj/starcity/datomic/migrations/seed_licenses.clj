(ns starcity.datomic.migrations.seed-licenses
  (:require [datomic.api :as d]))

(defn migration [conn]
  [{:db/id        (d/tempid :db.part/starcity)
    :license/term 1}
   {:db/id        (d/tempid :db.part/starcity)
    :license/term 6}
   {:db/id        (d/tempid :db.part/starcity)
    :license/term 12}])

(def norms
  {:starcity/seed-licenses {:txes     [migration]
                            :requires [:starcity/starcity-partition]}})
