(ns starcity.datomic.seed.accounts
  (:require [starcity.datomic.partition :refer [tempid]]
            [datomic.api :as d]))

(def password
  "bcrypt+blake2b-512$30e1776f40ee533841fcba62a0dbd580$12$2dae523ec1eb9fd91409ebb5ed805fe53e667eaff0333243")

(defn account
  [email first-name last-name phone role]
  {:db/id                (tempid)
   :account/email        email
   :account/password     password
   :account/first-name   first-name
   :account/last-name    last-name
   :account/phone-number phone
   :account/role         role
   :account/activated    true})

(def tx-data
  [(account "test@test.com" "Applicant" "User" "5103817881" :account.role/applicant)
   (account "member@test.com" "Member" "User" "5103817881" :account.role/tenant)
   (account "onboarding@test.com" "Onboarding" "User" "5103817881" :account.role/pending)
   (account "admin@test.com" "Admin" "User" "5103817881" :account.role/admin)])

(defn seed [conn]
  @(d/transact conn tx-data))
