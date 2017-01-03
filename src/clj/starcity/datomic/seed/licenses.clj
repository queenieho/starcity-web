(ns starcity.datomic.seed.licenses
  (:require [starcity.datomic.partition :refer [tempid]]
            [datomic.api :as d]))

(defn seed [conn]
  @(d/transact conn [{:db/id (tempid) :license/term 1 :license/available false}
                     {:db/id (tempid) :license/term 3}
                     {:db/id (tempid) :license/term 6 :license/available true}
                     {:db/id (tempid) :license/term 12}]))
