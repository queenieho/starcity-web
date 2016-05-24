(ns starcity.models.account
  (:require [buddy.hashers :as hashers]
            [clojure.string :refer [trim lower-case]]
            [datomic.api :as d]
            [starcity.datomic.util :refer [one]]
            [starcity.models.util :refer :all]))

;; =============================================================================
;; Password Hashing

(defn- hash-password [password]
  (hashers/derive password {:alg :bcrypt+blake2b-512 :iterations 12}))

(defn- check-password [password hash]
  (hashers/check password hash))

;; =============================================================================
;; API

(defn create!
  "Create a new user record in the database, and return the user's id upon
  successful creation."
  [{:keys [conn part] :as db} email password first-name last-name]
  (let [account (mapify :account {:first-name (trim first-name)
                                  :last-name  (trim last-name)
                                  :email      (-> email trim lower-case)
                                  :password   (-> password trim hash-password)
                                  :activated  false})
        tid     (d/tempid part)
        tx      @(d/transact conn [(assoc account :db/id tid)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))

(defn exists?
  [{:keys [conn]} email]
  (one (d/db conn) :account/email email))

(defn authenticate
  "Return the user record found under `username' iff a user record exists for
  that username and the password matches."
  [{:keys [conn] :as db} email password]
  (when-let [user (one (d/db conn) :account/email email)]
    (when (check-password password (:account/password user))
      user)))

;; =============================================================================
;; Component

;; (defrecord Account [datomic]
;;   )

(comment

  (def db* (:datomic user/system))

  (create! db* "Josh" "Lehman" "josh@starcity.com" "password")

  )
