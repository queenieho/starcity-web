(ns starcity.models.account
  (:require [buddy.hashers :as hashers]
            [buddy.core.hash :refer [md5]]
            [buddy.core.codecs :refer [bytes->hex]]
            [clojure.string :refer [trim lower-case]]
            [datomic.api :as d]
            [starcity.datomic.util :refer [one]]
            [starcity.models.util :refer :all]
            [starcity.datomic :refer [conn] :as db]))

;; =============================================================================
;; Helpers

(defn- generate-activation-hash
  [email]
  (-> email
      (str (System/currentTimeMillis))
      (md5)
      (bytes->hex)))

;; =============================================================================
;; Password Hashing

(defn- hash-password [password]
  (hashers/derive password {:alg :bcrypt+blake2b-512 :iterations 12}))

(defn- check-password [password hash]
  (hashers/check password hash))

;; =============================================================================
;; API

(defn query
  [pattern entity-id]
  (d/pull (d/db conn) pattern entity-id))

(defn create!
  "Create a new user record in the database, and return the user's id upon
  successful creation."
  [email password first-name last-name]
  (let [acct (mapify :account
                     {:first-name      (trim first-name)
                      :last-name       (trim last-name)
                      :email           (-> email trim lower-case)
                      :password        (-> password trim hash-password)
                      :activation-hash (generate-activation-hash email)
                      :activated       false})
        tid  (d/tempid db/partition)
        tx   @(d/transact conn [(assoc acct :db/id tid)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))

(defn exists?
  "Returns an account iff one exists under this username, and nil otherwise."
  [email]
  (one (d/db conn) :account/email email))

(def by-email
  "More semantic way to search for an user by email than using the `exists?'
  function."
  exists?)

(defn activate!
  [account]
  (let [ent {:db/id (:db/id account) :account/activated true}]
    @(d/transact conn [ent])))

(defn authenticate
  "Return the user record found under `username' iff a user record exists for
  that username and the password matches."
  [email password]
  (when-let [user (one (d/db conn) :account/email email)]
    (when (check-password password (:account/password user))
      user)))
