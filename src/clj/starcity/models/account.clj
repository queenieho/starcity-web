(ns starcity.models.account
  (:require [clojure
             [spec :as s]
             [string :refer [capitalize lower-case trim]]]
            [datomic.api :as d]
            [potemkin :refer [import-vars]]
            [starcity spec
             [datomic :refer [conn tempid]]]
            [starcity.util :refer [entity? conn?]]
            [starcity.spec.datomic]
            [starcity.models.account
             [auth :as auth]
             [role :as r]]))

;; =============================================================================
;; Selectors

(def email :account/email)
(def phone-number :account/phone-number)
(def first-name :account/first-name)
(def middle-name :account/middle-name)
(def last-name :account/last-name)
(def dob :account/dob)
(def activation-hash :account/activation-hash)
(def member-application :account/member-application)
(def role :account/role)

(defn full-name
  "Full name of person identified by this account."
  [{:keys [:account/first-name :account/last-name :account/middle-name]}]
  (if (not-empty middle-name)
    (format "%s %s %s" first-name middle-name last-name)
    (format "%s %s" first-name last-name)))

;; TODO: `conn` as arg
(defn stripe-customer
  "Retrieve the `stripe-customer` that belongs to this account. Produces the
  customer that is on the Stripe master account, NOT the managed one -- the
  customer on the managed account will be used *only* for autopay."
  [account]
  (when-let [c (d/q '[:find ?e .
                      :in $ ?a
                      :where
                      [?e :stripe-customer/account ?a]
                      [(missing? $ ?e :stripe-customer/managed)]]
                    (d/db conn) (:db/id account))]
    (d/entity (d/db conn) c)))

;; TODO: `conn` as arg
(defn created-at
  "Find the time that this account was created at.

  This is accomplished by looking at `db/txInstant` of the first transaction
  associated with `:account/email`."
  [account]
  (-> (d/q '[:find [?tx-time ...]
             :in $ ?e
             :where
             [?e :account/email _ ?t]
             [?t :db/txInstant ?tx-time]]
           (d/db conn) (:db/id account))
      (sort)
      (first)))

(s/fdef created-at
        :args (s/cat :account entity?)
        :ret inst?)

;; =============================================================================
;; Predicates

;; TODO: `conn` as arg
(defn exists? [email]
  (d/entity (d/db conn) [:account/email email]))

;; =============================================================================
;; Queries

;; TODO: `conn` as arg
(defn by-email [email]
  (d/entity (d/db conn) [:account/email email]))

(defn by-customer-id [conn customer-id]
  (:stripe-customer/account
   (d/entity (d/db conn) [:stripe-customer/customer-id customer-id])))

;; =============================================================================
;; Transactions

;; TODO: Don't transact here
(defn create
  "Create a new user record in the database, and return the user's id upon
  successful creation."
  [email password first-name last-name]
  (let [acct {:account/first-name      (-> first-name trim capitalize)
              :account/last-name       (-> last-name trim capitalize)
              :account/email           (-> email trim lower-case)
              :account/password        (-> password trim auth/hash-password)
              :account/activation-hash (auth/activation-hash email)
              :account/activated       false
              :account/role            r/applicant}
        tid  (tempid)
        tx   @(d/transact conn [(assoc acct :db/id tid)])]
    (->> (d/resolve-tempid (d/db conn) (:tempids tx) tid)
         (d/entity (d/db conn)))))

(s/fdef create
        :args (s/cat :email string?
                     :password string?
                     :first-name string?
                     :last-name string?)
        :ret :starcity.spec.datomic/entity)

;; TODO: Don't transact here
(defn activate
  "Indicate that the user has successfully verified ownership over the provided
  email address."
  [account]
  (:db-after @(d/transact conn [{:db/id             (:db/id account)
                                 :account/activated true}])))

(s/fdef activate
        :args (s/cat :account :starcity.spec.datomic/entity)
        :ret :starcity.spec.datomic/db)

;;; Convenience API

(import-vars
 [starcity.models.account.role
  applicant
  member
  admin
  onboarding
  applicant?
  member?
  admin?
  onboarding?
  change-role]
 [starcity.models.account.auth
  change-password
  reset-password
  authenticate
  session-data
  is-password?])
