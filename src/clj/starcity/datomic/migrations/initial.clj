(ns starcity.datomic.migrations.initial
  (:require [starcity.datomic.migrations.utils :refer :all]
            [starcity.datomic.migrations.initial
             [add-account-roles :refer [add-account-roles]]
             [add-account-schema :refer [add-account-schema]]
             [add-address-schema :refer [add-address-schema]]
             [add-charge-schema :refer [add-charge-schema]]
             [add-community-fitness-schema :refer [add-community-fitness-schema]]
             [add-community-safety-schema :refer [add-community-safety-schema]]
             [add-license-schema :refer [add-license-schema]]
             [add-member-application-schema :refer [add-member-application-schema]]
             [add-member-license-schema :refer [add-member-license-schema]]
             [add-pet-schema :refer [add-pet-schema]]
             [add-plaid-schema :refer [add-plaid-schema]]
             [add-property-license-schema :refer [add-property-license-schema]]
             [add-property-schema :refer [add-property-schema]]
             [add-starcity-partition :refer [add-starcity-partition]]
             [add-unit-schema :refer [add-unit-schema]]
             [seed-gilbert :refer [seed-gilbert]]
             [seed-historic-tenderloin :refer [seed-historic-tenderloin]]
             [seed-licenses :refer [seed-licenses]]
             [seed-mission :refer [seed-mission]]
             [seed-test-accounts :refer [seed-test-accounts]]
             [seed-union-square :refer [seed-union-square]]]))

(defn initial-migration [conn]
  (merge
   add-starcity-partition
   add-account-roles
   add-account-schema
   add-address-schema
   add-charge-schema
   add-community-fitness-schema
   add-community-safety-schema
   add-license-schema
   add-member-application-schema
   add-member-license-schema
   add-pet-schema
   add-plaid-schema
   add-property-license-schema
   add-property-schema
   add-unit-schema
   seed-licenses
   seed-gilbert
   seed-historic-tenderloin
   seed-mission
   (only-when #{:development :staging} seed-test-accounts)
   seed-union-square))
