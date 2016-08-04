(ns starcity.datomic.migrations.initial.seed-test-accounts
  (:require [starcity.datomic.migrations.utils :refer :all]
            [starcity.datomic.migrations :refer [defnorms]]))

(defnorms seed-test-accounts
  :txes (add-tempids
         [{:account/email      "test@test.com"
           :account/password   "bcrypt+blake2b-512$30e1776f40ee533841fcba62a0dbd580$12$2dae523ec1eb9fd91409ebb5ed805fe53e667eaff0333243"
           :account/first-name "Josh"
           :account/last-name  "Lehman"
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
           :account/first-name "Josh"
           :account/last-name  "Lehman"
           :account/role       :account.role/admin
           :account/activated  true}])
  :env  #{:development :staging}
  :requires [add-starcity-partition add-account-roles add-account-schema])
