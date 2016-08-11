(ns starcity.datomic.migrations.initial.seed-licenses
  (:require [datomic.api :as d]))

(def seed-licenses
  {:starcity/seed-licenses
   {:txes     [[{:db/id        (d/tempid :db.part/starcity)
                 :license/term 1}
                {:db/id        (d/tempid :db.part/starcity)
                 :license/term 6}
                {:db/id        (d/tempid :db.part/starcity)
                 :license/term 12}]]
    :requires [:starcity/add-starcity-partition]}})
