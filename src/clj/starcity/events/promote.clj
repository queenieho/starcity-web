(ns starcity.events.promote
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure.spec :as s]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when]]
            [starcity
             [datomic :refer [conn]]
             [util :refer :all]]
            [starcity.events.plumbing :refer [defproducer]]
            [starcity.models
             [account :as account]
             [license :as license]
             [member-license :as member-license]
             [news :as news]
             [rent-payment :as rent-payment]
             [security-deposit :as security-deposit]]))

(defn- assert-onboarded
  "Asserts that `account` has a paid security deposit and currently has the
  onboarding status."
  [account]
  (let [sd (security-deposit/by-account account)]
    (assert (security-deposit/is-paid? sd)
            "Cannot promote an account with an unpaid security deposit.")
    (assert (account/onboarding? account)
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
  (let [deposit  (security-deposit/by-account account)
        due-date (-> (t/plus (c/to-date-time (end-of-day commencement))
                             (t/days 30))
                     c/to-date)]
    {:db/id                   (:db/id deposit)
     :security-deposit/due-by due-date}))

(defn member-tx
  "All of the transaction data needed to transition an account smoothly from
  onboarding to membership."
  [conn account license unit commencement rate]
  (let [base           (account/change-role account account/member)
        member-license (member-license/create license unit commencement rate)
        payment        (when-not (rent-payment/payment-within conn account (java.util.Date.))
                         (prorated-payment commencement rate))]
    [(->> (assoc-when member-license :member-license/rent-payments payment)
          (assoc base :account/license))
     (security-deposit-due-date conn account commencement)
     (news/welcome account)
     (news/autopay account)]))

(defproducer to-member! ::to-member
  [account license unit commencement rate]
  @(d/transact conn (member-tx conn account license unit commencement rate)))

(s/fdef to-member!
        :args (s/cat :account entity?
                     :license entity?
                     :unit entity?
                     :commencement inst?
                     :rate float?)
        :ret chan?)

(comment

  (let [conn starcity.datomic/conn]
    (to-member!
     (account/by-email "jon@test.com")
     (license/by-term conn 6)
     (d/entity (d/db conn) [:unit/name "52gilbert-1"])
     (java.util.Date.)
     2000.0))

  )
