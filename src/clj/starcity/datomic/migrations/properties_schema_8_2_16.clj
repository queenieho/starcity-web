(ns starcity.datomic.migrations.properties-schema-8-2-16
  (:require [starcity.datomic.migrations :refer [defmigration]]))

(defmigration properties-schema-8-2-16 :requires [initial-migration]
   update-properties-description-copy)
