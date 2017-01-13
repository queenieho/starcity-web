(ns starcity.seeder
  (:require [mount.core :refer [defstate]]
            [starcity
             [datomic :refer [conn]]
             [environment :as env]]
            [starcity.datomic
             [migrata :as migrata]
             [seed :as seed]]))

(defstate ^{:on-reload :noop} seeder
  :start (do
           (when-not (env/is-production?)
             (seed/seed conn))
           (migrata/migrate conn)))
