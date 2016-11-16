(ns starcity.datomic.migrations
  (:require [starcity.datomic.migrations
             [initial :refer [initial-migration]]
             [update-properties-descriptions-8-2-16 :refer [update-properties-descriptions]]
             [income-files-8-3-16 :refer [add-income-files-schema]]
             [seed-test-applications-8-4-16 :refer [seed-test-applications]]
             [add-account-role-pending-8-18-16 :refer [add-account-role-pending]]
             [add-security-deposit-schema-8-18-16 :refer [add-security-deposit-schema]]
             [add-stripe-customer-schema-8-30-16 :refer [add-stripe-customer-schema]]
             [onboarding-updates-9-8-16 :refer [add-stripe-credentials-to-property-schema
                                                seed-stripe-test-credentials]]
             [add-approval-schema-9-8-16 :refer [add-approval-schema
                                                 seed-test-approval]]
             [add-community-safety-consent-9-28-16 :refer [add-community-safety-consent]]
             [add-has-pet-attr-10-3-16 :refer [add-has-pet-attr
                                               seed-has-pet]]
             [alter-address-schema-10-8-16 :refer [alter-address-schema]]
             [add-stripe-event-schema-11-1-16 :refer [add-stripe-event-schema]]
             [add-charge-status-11-2-16 :refer [add-charge-status]]
             [alter-security-deposit-schema-11-2-16 :refer [alter-security-deposit-schema]]
             [add-check-schema-11-4-16 :refer [add-check-schema
                                               add-checks-to-security-deposit-schema]]
             [add-member-application-status-11-15-16 :refer [add-member-application-status
                                                             seed-member-application-statuses]]]
            [starcity.datomic.migrations.utils :refer [only-when]]
            [starcity.environment]
            [mount.core :refer [defstate]]))

(defn migration-norms [conn]
  (merge
   (initial-migration conn)
   update-properties-descriptions
   add-income-files-schema
   (only-when #{:development :staging} seed-test-applications)
   add-account-role-pending
   add-security-deposit-schema
   add-stripe-customer-schema
   add-stripe-credentials-to-property-schema
   (only-when #{:development :staging} seed-stripe-test-credentials)
   add-approval-schema
   (only-when #{:development :staging} seed-test-approval)
   add-community-safety-consent
   add-has-pet-attr
   seed-has-pet
   alter-address-schema
   add-stripe-event-schema
   add-charge-status
   alter-security-deposit-schema
   add-check-schema
   add-checks-to-security-deposit-schema
   add-member-application-status
   seed-member-application-statuses))
