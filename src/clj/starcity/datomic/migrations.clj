(ns starcity.datomic.migrations
  (:require [starcity.datomic.migrations
             [initial :refer [initial-migration]]
             [update-properties-descriptions-8-2-16 :refer [update-properties-descriptions]]
             [income-files-8-3-16 :refer [add-income-files-schema]]
             [seed-test-applications-8-4-16 :refer [seed-test-applications]]]
            [starcity.datomic.migrations.utils :refer [only-when]]
            [starcity.environment]
            [mount.core :refer [defstate]]))

(defn migration-norms [conn]
  (merge
   (initial-migration conn)
   update-properties-descriptions
   add-income-files-schema
   (only-when #{:development} seed-test-applications)))
