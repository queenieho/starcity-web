(ns starcity.models.approval
  (:require [clojure.spec :as s]
            [starcity.datomic :refer [tempid]]
            [starcity.models
             [application :as app]
             [cmd :as cmd]
             [msg :as msg]
             [security-deposit :as deposit]
             [unit :as unit]]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Lookups
;; =============================================================================

(def by-account
  "Look up the `approval` entity by `account`."
  (comp first :approval/_account))

(s/fdef by-account
        :args (s/cat :account p/entity?)
        :ret p/entity?)

;; =============================================================================
;; Selectors
;; =============================================================================

(def approver
  "The admin `account` that did the approving."
  :approval/approver)

(s/fdef approver
        :args (s/cat :approval p/entity?)
        :ret p/entity?)

(def move-in
  "The move-in date."
  :approval/move-in)

(s/fdef move-in
        :args (s/cat :approval p/entity?)
        :ret inst?)

(def unit
  "The `unit` that `account` is approved to live in."
  :approval/unit)

(s/fdef unit
        :args (s/cat :approval p/entity?)
        :ret p/entity?)

(def license
  "The `license` (term) that `account` was approved for."
  :approval/license)

(s/fdef license
        :args (s/cat :approval p/entity?)
        :ret p/entity?)

;; =============================================================================
;; Transactions
;; =============================================================================

(defn create
  "Produce transaction data required to create an approval entity."
  [approver approvee unit license move-in]
  {:db/id             (tempid)
   :approval/account  (:db/id approvee)
   :approval/approver (:db/id approver)
   :approval/unit     (:db/id unit)
   :approval/license  (:db/id license)
   :approval/move-in  move-in
   :approval/status   :approval.status/pending})

(s/fdef create
        :args (s/cat :approver p/entity?
                     :approvee p/entity?
                     :unit p/entity?
                     :license p/entity?
                     :move-in inst?)
        :ret (s/keys :req [:db/id
                           :approval/account
                           :approval/approver
                           :approval/unit
                           :approval/license
                           :approval/move-in
                           :approval/status]))

(defn approve
  "Approve `approvee` by creating an `approval` entity and flipping the
  necessary bits elswhere in the database.

  More specifically, this means:
  - Change `account`'s role to onboarding
  - Create a security deposit stub
  - Mark the application as approved"
  [approver approvee unit license move-in]
  [(create approver approvee unit license move-in)
   ;; Change role
   {:db/id (:db/id approvee) :account/role :account.role/onboarding}
   (deposit/create approvee (int (unit/rate unit license)))
   (app/change-status (:account/application approvee)
                      :application.status/approved)
   (msg/approved approver approvee unit license move-in)
   ;; Log `approvee` out
   (cmd/delete-session approvee)])

(s/fdef approve
        :args (s/cat :approver p/entity?
                     :approvee p/entity?
                     :unit p/entity?
                     :license p/entity?
                     :move-in inst?)
        :ret (s/and vector? (s/+ map?)))
