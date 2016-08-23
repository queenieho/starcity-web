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
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [taoensso.timbre :as timbre]))

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

;; =============================================================================
;; Passwords

(defn- hash-password [password]
  (hashers/derive password {:alg :bcrypt+blake2b-512 :iterations 12}))

(defn- check-password [password hash]
  (hashers/check password hash))

(defn generate-random-password
  ([]
   (generate-random-password 8))
  ([n]
   (let [chars    (concat
                   (map char (range 48 58))
                   (map char (range 65 91))
                   (map char (range 97 123)))
         password (take n (repeatedly #(rand-nth chars)))]
     (reduce str password))))

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
        tid  (tempid)
        tx   @(d/transact conn [(assoc acct :db/id tid)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))

(defn activate!
  [account]
  (let [ent {:db/id (:db/id account) :account/activated true}]
    @(d/transact conn [ent])))

(def update!
  (make-update-fn
   {:dob          (fn [{id :db/id} {dob :dob}]
                    [[:db/add id :account/dob dob]])
    :name         (fn [{id :db/id} {{:keys [first middle last]} :name}]
                    (map-form->list-form id {:account/first-name  first
                                             :account/last-name   last
                                             :account/middle-name middle}))
    :phone-number (fn [{id :db/id} {phone-number :phone-number}]
                    [[:db/add id :account/phone-number phone-number]])}))

(defn- write-income-file
  "Write a an income file to the filesystem and add an entity that points to the
  account and file path."
  [account-id {:keys [filename content-type tempfile size]}]
  (try
    (let [output-dir  (format "%s/income-uploads/%s" (:data-dir config/config) account-id)
          output-path (str output-dir "/" filename)]
      (do
        (when-not (fs/exists? output-dir)
          (fs/mkdirs output-dir))
        (io/copy tempfile (java.io.File. output-path))
        @(d/transact conn [{:income-file/account     account-id
                            :income-file/content-type content-type
                            :income-file/path         output-path
                            :income-file/size         (long size)
                            :db/id                    (tempid)}])
        (timbre/infof "Wrote income file for account-id %s - %s - %s - %d"
                      account-id filename content-type size)))
    (catch Exception e
      (timbre/error e "Error encountered while writing income file!"
                    (format "%s - %s - %s - %d" account-id filename content-type size)))))

(defn save-income-files!
  "Save the income files for a given account."
  [account-id files]
  (dorun (map (partial write-income-file account-id) files)))

;; =============================================================================
;; Misc

(defn session-data
  [ent]
  {:account/email      (:account/email ent)
   :account/role       (:account/role ent)
   :account/activated  (:account/activated ent)
   :account/first-name (:account/first-name ent)
   :account/last-name  (:account/last-name ent)
   :db/id              (:db/id ent)})

(defn authenticate
  "Return the user record found under `username' iff a user record exists for
  that username and the password matches."
  [email password]
  (when-let [acct (one (d/db conn) :account/email email)]
    (when (check-password password (:account/password acct))
      (session-data acct))))

(defn change-password!
  [account-id new-password]
  @(d/transact conn [{:db/id account-id :account/password (hash-password new-password)}]))

(defn reset-password!
  [account-id]
  (let [new-password (generate-random-password)]
    (change-password! account-id new-password)
    new-password))

(defn applicant?
  [account]
  (= (:account/role account) :account.role/applicant))

(defn admin?
  [account]
  (= (:account/role account) :account.role/admin))

(defn full-name
  "Full name of this account."
  [{:keys [:account/first-name :account/last-name :account/middle-name]}]
  (if (not-empty middle-name)
    (format "%s %s %s" first-name middle-name last-name)
    (format "%s %s" first-name last-name)))

(defn is-password?
  [account-id password]
  (let [hash (:account/password (d/entity (d/db conn) account-id))]
    (check-password password hash)))
