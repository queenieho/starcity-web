(ns starcity.datomic.partition
  (:require [datomic.api :as d]))

(def part :db.part/starcity)

(defn tempid
  ([] (d/tempid part))
  ([n] (d/tempid part n)))
