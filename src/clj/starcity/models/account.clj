(ns starcity.models.account
  (:require [buddy.core
             [codecs :refer [bytes->hex]]
             [hash :refer [md5]]]
            [buddy.hashers :as hashers]
            [clojure.string :refer [lower-case trim]]
            [datomic.api :as d]
            [starcity
             [config :as config]
             [datomic :refer [conn tempid]]]
            [starcity.models.util :refer :all]
            [starcity.models.util.update :refer [make-update-fn]]
            [starcity.spec]
            [clojure.spec :as s]))

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
(derive :account.role/admin :account.role/pending)

(s/def ::role
  #{:account.role/admin
    :account.role/pending
    :account.role/applicant
    :account.role/tenant})

(def admin-role :account.role/admin)
(def onboarding-role :account.role/pending)
(def applicant-role :account.role/applicant)
(def member-role :account.role/tenant)

;; =============================================================================
;; Selectors

(def email :account/email)
(def first-name :account/first-name)
(def middle-name :account/middle-name)
(def last-name :account/last-name)
(def dob :account/dob)
(def activation-hash :account/activation-hash)
(def member-application :account/member-application)

(defn full-name
  "Full name of this account."
  [{:keys [:account/first-name :account/last-name :account/middle-name]}]
  (if (not-empty middle-name)
    (format "%s %s %s" first-name middle-name last-name)
    (format "%s %s" first-name last-name)))

;; =============================================================================
;; Passwords

(defn- hash-password [password]
  (hashers/derive password {:alg :bcrypt+blake2b-512 :iterations 12}))

(defn- check-password [password hash]
  (hashers/check password hash))

(defn generate-random-password
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

(defn change-password
  "Change the password for `account` to `new-password`. `new-password` will be
  hashed before it is saved to db."
  [account new-password]
  @(d/transact conn [{:db/id (:db/id account) :account/password (hash-password new-password)}]))

(s/fdef change-password
        :args (s/cat :account :starcity.spec/entity
                     :new-password string?))

(defn reset-password
  "Reset the password for `account` by generating a random password. Return the
  generated password."
  [account]
  (let [new-password (generate-random-password)]
    (change-password account new-password)
    new-password))

;; =============================================================================
;; Predicates

(defn exists?
  "Returns an account iff one exists under this username, and nil otherwise."
  [email]
  (d/entity (d/db conn) [:account/email email]))

(defn applicant?
  [account]
  (= (:account/role account) :account.role/applicant))

(defn admin?
  [account]
  (= (:account/role account) :account.role/admin))

(defn tenant?
  [account]
  (= (:account/role account) :account.role/tenant))

(defn is-password?
  [account password]
  (let [hash (:account/password account)]
    (check-password password hash)))

;; =============================================================================
;; Lookups

(def by-email
  "More semantic way to search for an user by email than using the `exists?'
  function."
  exists?)

(defn by-application
  "Given a member application entity, produce the associated account."
  [application]
  (first (:account/_member-application application)))

;; =============================================================================
;; Transactions

(defn create
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
        tid  (tempid)
        tx   @(d/transact conn [(assoc acct :db/id tid)])]
    (d/entity (d/db conn) (d/resolve-tempid (d/db conn) (:tempids tx) tid))))

(defn activate
  "Activate the account by setting the `:account/activated` flag to true."
  [account]
  (let [ent {:db/id (:db/id account) :account/activated true}]
    @(d/transact conn [ent])))

;; =============================================================================
;; Authentication

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
  "Return the user record found under `username' iff a user record exists for
  that username and the password matches."
  [email password]
  (when-let [acct (one (d/db conn) :account/email email)]
    (when (check-password password (:account/password acct))
      (session-data acct))))



(comment

  (require '[clj-time.core :as t])

  (require '[clj-time.coerce :as c])

  (defn- in-september? [t]
    (t/within? (t/interval (t/date-time 2016 9 1) (t/date-time 2016 9 30)) t))

  (defn- in-october? [t]
    (t/within? (t/interval (t/date-time 2016 10 1) (t/date-time 2016 10 31)) t))

  (defn- in-november? [t]
    (t/within? (t/interval (t/date-time 2016 11 1) (t/date-time 2016 11 30)) t))

  (defn- accounts-created []
    (->> (d/q '[:find ?tx-time
                :where
                [?e :account/email _ ?tx]
                [?tx :db/txInstant ?tx-time]]
              (d/db conn))
         (map (comp c/from-date first))))

  (defn- applications-created []
    (->> (d/q '[:find ?tx-time
                :where
                [?e :member-application/desired-availability _ ?tx]
                [?tx :db/txInstant ?tx-time]]
              (d/db conn))
         (map (comp c/from-date first))))

  {:accounts
   {:september (->> (accounts-created)
                    (filter in-september?)
                    (count))
    :october   (->> (accounts-created)
                    (filter in-october?)
                    (count))
    :november  (->> (accounts-created)
                    (filter in-november?)
                    (count))}
   :applications
   {:september (->> (applications-created)
                    (filter in-september?)
                    (count))
    :october   (->> (applications-created)
                    (filter in-october?)
                    (count))
    :november  (->> (applications-created)
                    (filter in-november?)
                    (count))}}


  )
