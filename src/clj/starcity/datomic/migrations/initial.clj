(ns starcity.datomic.migrations.initial
  (:require [starcity.datomic.migrations :refer [defmigration]]))

(defmigration initial-migration
  add-account-roles
  add-account-schema
  add-address-schema
  add-charge-schema
  add-community-fitness-schema
  add-license-schema
  add-member-application-schema
  add-member-license-schema
  add-pet-schema
  add-plaid-schema
  add-property-license-schema
  add-property-schema
  add-unit-schema
  add-starcity-partition
  seed-licenses
  seed-gilbert
  seed-historic-tenderloin
  seed-union-square
  seed-mission
  seed-test-accounts)
