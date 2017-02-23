(ns starcity.models.account.auth
  (:require [buddy.core
             [codecs :refer [bytes->hex]]
             [hash :refer [md5]]]
            [buddy.hashers :as hashers]
            [clojure.spec :as s]
            [datomic.api :as d]
            [starcity.spec.datomic]
            [starcity spec
             [datomic :refer [conn]]]))

(defn hash-password [password]
  (hashers/derive password {:alg :bcrypt+blake2b-512 :iterations 12}))

(defn- check-password [password hash]
  (hashers/check password hash))

(defn- generate-random-password
  "With no args, produces a random string of 8 characters. `n` can optionally be
  specified."
  ([]
   (generate-random-password 8))
  ([n]
   (let [chars    (concat
                   (map char (range 48 58))
                   (map char (range 65 91))
                   (map char (range 97 123)))
         password (take n (repeatedly #(rand-nth chars)))]
     (reduce str password))))

(defn activation-hash [email]
  (-> email (str (System/currentTimeMillis)) (md5) (bytes->hex)))

(defn change-password
  "Change the password for `account` to `new-password`. `new-password` will be
  hashed before it is saved to db."
  [account new-password]
  (:db-after
   @(d/transact conn [{:db/id (:db/id account) :account/password (hash-password new-password)}])))

(s/fdef change-password
        :args (s/cat :account :starcity.spec/entity
                     :new-password string?)
        :ret :starcity.spec.datomic/db)

(defn reset-password
  "Reset the password for `account` by generating a random password. Return the
  generated password."
  [account]
  (let [new-password (generate-random-password)]
    (do
      (change-password account new-password)
      new-password)))

(s/fdef reset-password
        :args (s/cat :account :starcity.spec.datomic/entity)
        :ret string?)

(defn is-password?
  "Does `password` the correct password for this `account`?"
  [account password]
  (let [hash (:account/password account)]
    (check-password password hash)))

(s/fdef is-password?
        :args (s/cat :account :starcity.spec.datomic/entity
                     :password string?)
        :ret boolean?)

;; TODO: Remove
(defn session-data
  "Produce the data that should be stored in the session for `account`."
  [account]
  {:account/email      (:account/email account)
   :account/role       (:account/role account)
   :account/activated  (:account/activated account)
   :account/first-name (:account/first-name account)
   :account/last-name  (:account/last-name account)
   :db/id              (:db/id account)})

(defn authenticate
  "Return the user record found under `email` iff a user record exists for
  that email and the `password` matches."
  [email password]
  (when-let [acct (d/entity (d/db conn) [:account/email email])]
    (when (check-password password (:account/password acct))
      (session-data acct))))

(s/fdef authenticate
        :args (s/cat :email string? :password string?)
        :ret :starcity.spec.datomic/entity)
