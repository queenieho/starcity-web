(ns starcity.datomic.migrations.seed-test-accounts-2016-07-27-18-31-45
  (:require [starcity.datomic.migration-utils :refer :all]))

(def migration
  (add-tempids
   [{:account/email      "test@test.com"
     :account/password   "bcrypt+blake2b-512$30e1776f40ee533841fcba62a0dbd580$12$2dae523ec1eb9fd91409ebb5ed805fe53e667eaff0333243"
     :account/first-name "josh"
     :account/last-name  "lehman"
     :account/role       :account.role/applicant
     :account/activated  true}

    {:account/email      "unactivated@test.com"
     :account/password   "bcrypt+blake2b-512$30e1776f40ee533841fcba62a0dbd580$12$2dae523ec1eb9fd91409ebb5ed805fe53e667eaff0333243"
     :account/first-name "Test"
     :account/last-name  "User"
     :account/role       :account.role/applicant
     :account/activated  false}

    {:account/email      "tenant@test.com"
     :account/password   "bcrypt+blake2b-512$30e1776f40ee533841fcba62a0dbd580$12$2dae523ec1eb9fd91409ebb5ed805fe53e667eaff0333243"
     :account/first-name "Mo"
     :account/last-name  "Sakrani"
     :account/role       :account.role/tenant
     :account/activated  true}

    {:account/email      "admin@test.com"
     :account/password   "bcrypt+blake2b-512$30e1776f40ee533841fcba62a0dbd580$12$2dae523ec1eb9fd91409ebb5ed805fe53e667eaff0333243"
     :account/first-name "Jon"
     :account/last-name  "Dishotsky"
     :account/role       :account.role/admin
     :account/activated  true}]))

(def norms
  {:starcity/seed-test-accounts-2016-07-27-18-31-45 {:txes [migration]
                                                     :env  #{:development :staging}
                                                     :requires [:starcity/starcity-partition
                                                                :starcity/account-roles-2016-07-26-22-46-16]}})
