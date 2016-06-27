(ns starcity.models.account
  (:require [starcity.datomic.util :refer :all]
            [starcity.models.util :refer :all]
            [starcity.datomic :refer [conn]]
            [starcity.config :as config]
            [buddy.hashers :as hashers]
            [buddy.core.hash :refer [md5]]
            [buddy.core.codecs :refer [bytes->hex]]
            [clojure.string :refer [trim lower-case]]
            [datomic.api :as d]))

;; =============================================================================
;; Helpers

(defn- generate-activation-hash
  [email]
  (-> email
      (str (System/currentTimeMillis))
      (md5)
      (bytes->hex)))

;; =============================================================================
;; Roles

(derive :account.role/admin :account.role/applicant)
(derive :account.role/admin :account.role/tenant)

;; =============================================================================
;; Password Hashing

(defn- hash-password [password]
  (hashers/derive password {:alg :bcrypt+blake2b-512 :iterations 12}))

(defn- check-password [password hash]
  (hashers/check password hash))

;; =============================================================================
;; API

;; =============================================================================
;; Queries

(defn exists?
  "Returns an account iff one exists under this username, and nil otherwise."
  [email]
  (one (d/db conn) :account/email email))

(def by-email
  "More semantic way to search for an user by email than using the `exists?'
  function."
  exists?)

;; =============================================================================
;; Transactions

(defn create!
  "Create a new user record in the database, and return the user's id upon
  successful creation."
  [email password first-name last-name]
  (let [acct (ks->nsks :account
                       {:first-name      (trim first-name)
                        :last-name       (trim last-name)
                        :email           (-> email trim lower-case)
                        :password        (-> password trim hash-password)
                        :activation-hash (generate-activation-hash email)
                        :activated       false
                        :role            :account.role/applicant})
        tid  (d/tempid (config/datomic-partition))
        tx   @(d/transact conn [(assoc acct :db/id tid)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))

(defn activate!
  [account]
  (let [ent {:db/id (:db/id account) :account/activated true}]
    @(d/transact conn [ent])))

(def update!
  (make-update-fn conn {:ssn  (fn [{id :db/id} {ssn :ssn}]
                                [[:db/add id :account/ssn ssn]])
                        :dob  (fn [{id :db/id} {dob :dob}]
                                [[:db/add id :account/dob dob]])
                        :name (fn [{id :db/id} {{:keys [first middle last]} :name}]
                                (map-form->list-form id {:account/first-name  first
                                                         :account/last-name   last
                                                         :account/middle-name middle}))}))

;; =============================================================================
;; Misc

(defn authenticate
  "Return the user record found under `username' iff a user record exists for
  that username and the password matches."
  [email password]
  (when-let [user (one (d/db conn) :account/email email)]
    (when (check-password password (:account/password user))
      user)))

(defn applicant?
  [account]
  (= (:account/role account) :account.role/applicant))
