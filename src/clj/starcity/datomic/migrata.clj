(ns starcity.datomic.migrata
  (:require [starcity.datomic.conformity :as c]
            [starcity.datomic.migrata
             [address :as address]
             [member-application :as member-application]]))

(defn- assemble-norms [conn]
  (merge (address/norms conn)
         (member-application/norms conn)))

(defn migrate
  "Run all data migrations that have not already been run. Should be called at
  application startup after all schema changes have been transacted."
  [conn]
  (c/ensure-conforms conn (assemble-norms conn)))
