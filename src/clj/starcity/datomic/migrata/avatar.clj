(ns starcity.datomic.migrata.avatar
  (:require [starcity.datomic.partition :refer [tempid]]))

(defn norms [conn]
  {:migration.avatar/seed
   {:txes [[{:db/id       (tempid)
             :avatar/name :system
             :avatar/url  "/assets/img/starcity-logo-black.png"}]]}})
