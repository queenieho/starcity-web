(ns starcity.datomic.seed
  (:require [starcity.datomic.seed
             [accounts :as accounts]
             [applications :as applications]
             [licenses :as licenses]
             [properties :as properties]]))

;; TODO: sample properties
;; TODO: test accounts & applications
;; TODO: starcity.datomic.migrations.onboarding-updates-9-8-16
;; TODO: starcity.datomic.migrations.add-approval-schema-9-8-16

(def seed-fns
  [accounts/seed
   licenses/seed
   properties/seed
   applications/seed])

(defn seed
  "Seed the database with sample data."
  [conn]
  (doseq [f seed-fns] (f conn)))
