(ns starcity.datomic.seed
  (:require [starcity.datomic.seed
             [accounts :as accounts]
             [approval :as approval]
             [applications :as applications]
             [avatar :as avatar]
             [licenses :as licenses]
             [properties :as properties]
             [stripe-customers :as customers]
             [sample-member :as sample-member]
             [rent-payments :as rent-payments]]))

(def seed-fns
  [accounts/seed
   avatar/seed
   licenses/seed
   properties/seed
   applications/seed
   approval/seed
   customers/seed
   sample-member/seed
   rent-payments/seed])

(defn seed
  "Seed the database with sample data."
  [conn]
  (doseq [f seed-fns] (f conn)))
