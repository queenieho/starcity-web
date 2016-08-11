(ns starcity.datomic.migrations.initial.seed-test-accounts
  (:require [starcity.datomic.migrations.utils :refer :all]))

(def seed-test-accounts
  {:starcity/seed-test-accounts
   {:txes [(add-tempids
            [{:account/email        "test@test.com"
              :account/password     "bcrypt+blake2b-512$30e1776f40ee533841fcba62a0dbd580$12$2dae523ec1eb9fd91409ebb5ed805fe53e667eaff0333243"
              :account/first-name   "Josh"
              :account/last-name    "Lehman"
              :account/phone-number "(510) 381-7881"
              :account/role         :account.role/applicant
              :account/activated    true}

             {:account/email      "unactivated@test.com"
              :account/password   "bcrypt+blake2b-512$30e1776f40ee533841fcba62a0dbd580$12$2dae523ec1eb9fd91409ebb5ed805fe53e667eaff0333243"
              :account/first-name "Test"
              :account/last-name  "User"
              :account/role       :account.role/applicant
              :account/activated  false}

             {:account/email        "tenant@test.com"
              :account/password     "bcrypt+blake2b-512$30e1776f40ee533841fcba62a0dbd580$12$2dae523ec1eb9fd91409ebb5ed805fe53e667eaff0333243"
              :account/first-name   "Mo"
              :account/last-name    "Sakrani"
              :account/phone-number "(516) 749-0046"
              :account/role         :account.role/tenant
              :account/activated    true}

             {:account/email      "admin@test.com"
              :account/password   "bcrypt+blake2b-512$30e1776f40ee533841fcba62a0dbd580$12$2dae523ec1eb9fd91409ebb5ed805fe53e667eaff0333243"
              :account/first-name "Josh"
              :account/last-name  "Lehman"
              :account/role       :account.role/admin
              :account/activated  true}])]
    :requires [:starcity/add-starcity-partition
               :starcity/add-account-roles
               :starcity/add-account-schema]}})
