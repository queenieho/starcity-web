(ns starcity.models.account
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure
             [spec :as s]
             [string :refer [capitalize lower-case trim]]]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when]]
            [potemkin :refer [import-vars]]
            [starcity.datomic :refer [conn tempid]]
            [starcity.models
             [application :refer [submitted?]]
             [member-license :as member-license]
             [news :as news]
             [rent-payment :as rent-payment]
             [security-deposit :as deposit]]
            [starcity.models.account
             [auth :as auth]
             [role :as r]]
            [starcity.util.date :refer [end-of-day]]
            [toolbelt
             [core :refer [round]]
             [predicates :as p :refer [entity?]]]))


;; =============================================================================
;; Specs
;; =============================================================================

(s/def :account/name string?)

;; =============================================================================
;; Imports
;; =============================================================================

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

;; =============================================================================
;; Selectors
;; =============================================================================

(def email :account/email)
(def phone-number :account/phone-number)
(def first-name :account/first-name)
(def middle-name :account/middle-name)
(def last-name :account/last-name)
(def dob :account/dob)
(def activation-hash :account/activation-hash)
(def member-application :account/member-application)
(def role :account/role)

(def security-deposit
  "Retrieve the `security-deposit` for `account`."
  (comp first :security-deposit/_account))

(s/fdef security-deposit
        :args (s/cat :account p/entity?)
        :ret p/entity?)

(defn full-name
  "Full name of person identified by this account."
  [{:keys [:account/first-name :account/last-name :account/middle-name]}]
  (if (not-empty middle-name)
    (format "%s %s %s" first-name middle-name last-name)
    (format "%s %s" first-name last-name)))

;; TODO: `db` as arg
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

;; TODO: `db` as arg
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
        :args (s/cat :account p/entity?)
        :ret inst?)

(defn approval
  "Produces the `approval` entity for `account`."
  [account]
  (-> account :approval/_account first))

(s/fdef approval
        :args (s/cat :account p/entity?)
        :ret p/entity?)

(def slack-handle
  "Produces the slack handle for this account."
  :account/slack-handle)

(s/fdef slack-handle
        :args (s/cat :account p/entity?)
        :ret (s/or :nothing nil? :handle string?))

;; =============================================================================
;; Predicates
;; =============================================================================

;; TODO: `db` as arg
(defn exists? [email]
  (d/entity (d/db conn) [:account/email email]))

(defn can-approve?
  "An account can be *approved* if the application is submitted and the account
  has the `:account.status/applicant` role."
  [account]
  (let [application (:account/application account)]
    (and (submitted? application) (r/applicant? account))))

(def bank-linked?
  "Is there a bank account linked to this account?"
  (comp not empty? :stripe-customer/_account))

;; =============================================================================
;; Queries
;; =============================================================================

;; TODO: `db` as arg
(defn by-email [email]
  (d/entity (d/db conn) [:account/email email]))

(defn by-customer-id [conn customer-id]
  (:stripe-customer/account
   (d/entity (d/db conn) [:stripe-customer/customer-id customer-id])))

;; =============================================================================
;; Transactions
;; =============================================================================

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
        :ret p/entity?)

;; TODO: Don't transact here
(defn activate
  "Indicate that the user has successfully verified ownership over the provided
  email address."
  [account]
  (:db-after @(d/transact conn [{:db/id             (:db/id account)
                                 :account/activated true}])))

(s/fdef activate
        :args (s/cat :account p/entity?))

;; =====================================
;; Promote to Membership

(defn- assert-onboarded
  "Asserts that `account` has a paid security deposit and currently has the
  onboarding status."
  [account]
  (let [deposit (deposit/by-account account)]
    (assert (deposit/is-paid? deposit)
            "Cannot promote an account with an unpaid security deposit.")
    (assert (onboarding? account)
            "Cannot promote a non-onboarding account.")))

(defn- prorated-amount [commencement rate]
  (let [commencement   (c/to-date-time commencement)
        days-in-month  (t/day (t/last-day-of-the-month commencement))
        ;; We inc the days-remaining so that the move-in day is included in the calculation
        days-remaining (inc (- days-in-month (t/day commencement)))]
    (round (* (/ rate days-in-month) days-remaining) 2)))

(defn- prorated-payment [commencement rate]
  (let [dt (c/to-date-time commencement)]
    (rent-payment/create (prorated-amount commencement rate)
                         commencement
                         (c/to-date (t/last-day-of-the-month dt))
                         :rent-payment.status/due
                         :due-date (c/to-date (t/plus dt (t/days 5))))))

(defn- security-deposit-due-date
  [conn account commencement]
  (let [deposit  (deposit/by-account account)
        due-date (-> (t/plus (c/to-date-time (end-of-day commencement))
                             (t/days 30))
                     c/to-date)]
    {:db/id                   (:db/id deposit)
     :security-deposit/due-by due-date}))

(defn promote-to-member
  "All of the transaction data needed to transition an account smoothly from
  onboarding to membership."
  [conn account license unit commencement rate]
  (let [base           (change-role account r/member)
        member-license (member-license/create license unit commencement rate :member-license.status/active)
        payment        (prorated-payment commencement rate)]
    [(->> (assoc-when member-license :member-license/rent-payments payment)
          (assoc base :account/license))
     (security-deposit-due-date conn account commencement)
     (news/welcome account)
     (news/autopay account)]))

(s/fdef promote-to-member
        :args (s/cat :conn p/conn?
                     :account p/entity?
                     :license p/entity?
                     :unit p/entity?
                     :commencement inst?
                     :rate float?)
        :ret vector?)

;; =============================================================================
;; Transformations
;; =============================================================================

(defn clientize
  "Produce a client-suitable representation of an `account` entity."
  [account]
  {:db/id        (:db/id account)
   :account/name (full-name account)})

(s/fdef clientize
        :args (s/cat :account p/entity?)
        :ret (s/keys :req [:db/id :account/name]))
