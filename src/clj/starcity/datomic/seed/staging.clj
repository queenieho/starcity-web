(ns starcity.datomic.seed.staging
  (:require [blueprints.models.approval :as approval]
            [blueprints.models.license :as license]
            [blueprints.models.onboard :as onboard]
            [blueprints.models.security-deposit :as deposit]
            [blueprints.models.unit :as unit]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [datomic.api :as d]
            [io.rkn.conformity :as conformity]
            [toolbelt.core :as tb]))

(def password
  "bcrypt+blake2b-512$30e1776f40ee533841fcba62a0dbd580$12$2dae523ec1eb9fd91409ebb5ed805fe53e667eaff0333243")

(defn account
  [email first-name last-name phone role & [slack-handle]]
  (tb/assoc-when
   {:db/id                (d/tempid :db.part/starcity)
    :account/email        email
    :account/password     password
    :account/first-name   first-name
    :account/last-name    last-name
    :account/phone-number phone
    :account/role         role
    :account/activated    true}
   :account/slack-handle slack-handle))

;; =============================================================================
;; Approval

(defn approve
  "A more light-weight version of `starcity.models.approval/approve` that
  doesn't create `msg` and `cmd`."
  [approver approvee unit license move-in]
  [(approval/create approver approvee unit license move-in)
   ;; Change role
   {:db/id (:db/id approvee) :account/role :account.role/onboarding}
   (deposit/create approvee (int (unit/rate unit license)))
   (onboard/create approvee)])

(defn approval-tx [conn]
  (concat
   (approve
    (d/entity (d/db conn) [:account/email "josh@joinstarcity.com"])
    (d/entity (d/db conn) [:account/email "jon@test.com"])
    (unit/by-name (d/db conn) "2072mission-10")
    (license/by-term (d/db conn) 3)
    (c/to-date (t/now)))
   (approve
    (d/entity (d/db conn) [:account/email "josh@joinstarcity.com"])
    (d/entity (d/db conn) [:account/email "jesse@test.com"])
    (unit/by-name (d/db conn) "2072mission-11")
    (license/by-term (d/db conn) 3)
    (c/to-date (t/now)))
   (approve
    (d/entity (d/db conn) [:account/email "josh@joinstarcity.com"])
    (d/entity (d/db conn) [:account/email "mo@test.com"])
    (unit/by-name (d/db conn) "2072mission-12")
    (license/by-term (d/db conn) 3)
    (c/to-date (t/now)))
   (approve
    (d/entity (d/db conn) [:account/email "josh@joinstarcity.com"])
    (d/entity (d/db conn) [:account/email "meg@test.com"])
    (unit/by-name (d/db conn) "2072mission-13")
    (license/by-term (d/db conn) 3)
    (c/to-date (t/now)))
   (approve
    (d/entity (d/db conn) [:account/email "josh@joinstarcity.com"])
    (d/entity (d/db conn) [:account/email "jp@test.com"])
    (unit/by-name (d/db conn) "2072mission-14")
    (license/by-term (d/db conn) 3)
    (c/to-date (t/now)))))

;; =============================================================================
;; Security Deposits

;; NOTE: Needed for membership

;; (defn- create-security-deposit [account]
;;   {:db/id                            (d/tempid :db.part/starcity)
;;    :security-deposit/account         account
;;    :security-deposit/amount-required 2000
;;    :security-deposit/amount-received 500
;;    :security-deposit/payment-method  :security-deposit.payment-method/ach
;;    :security-deposit/payment-type    :security-deposit.payment-type/partial})

;; (defn security-deposits-tx []
;;   (map
;;    (comp create-security-deposit (partial conj [:account/email]))
;;    ["jon@test.com"
;;     "jesse@test.com"
;;     "mo@test.com"
;;     "meg@test.com"
;;     "jp@test.com"]))

;; =============================================================================
;; API
;; =============================================================================

(defn norms [conn]
  (conformity/ensure-conforms
   conn {:staging.seed/test-accounts
         {:txes [[(account "jon@test.com" "Jon" "Dishotsky" "2345678910" :account.role/onboarding)
                  (account "jesse@test.com" "Jesse" "Suarez" "2345678910" :account.role/onboarding)
                  (account "mo@test.com" "Mo" "Sakrani" "2345678910" :account.role/onboarding)
                  (account "meg@test.com" "Meg" "Bell" "2345678910" :account.role/onboarding)
                  (account "jp@test.com" "Josh" "Petersen" "2345678910" :account.role/onboarding)]]}})
  (conformity/ensure-conforms
   conn {:staging.seed/test-approvals
         {:txes [(approval-tx conn)]}}))
