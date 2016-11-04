(ns admin.account.entry.security-deposit.subs
  (:require [admin.account.entry.security-deposit.db :as model :refer [root-db-key]]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 root-db-key
 (fn [db _]
   (get db root-db-key)))

(reg-sub
 ::security-deposit
 :<- [root-db-key]
 (fn [db _]
   (model/security-deposit db)))

(reg-sub
 :account.entry.security-deposit/overview
 :<- [::security-deposit]
 (fn [db _]
   db))

(reg-sub
 :account.entry.security-deposit/status
 :<- [::security-deposit]
 (fn [{:keys [amount-received amount-required] :as sd} _]
   (cond
     (zero? amount-received)             :unpaid
     (empty? sd)                         :unavailable
     (= amount-received amount-required) :paid
     (> amount-received 0)               :partial
     :otherwise                          :unknown)))

(def ^:private stripe-payments-base-url
  "https://dashboard.stripe.com/payments")

(defn- charge-url [{id :id}]
  (str stripe-payments-base-url "/" id))

(reg-sub
 :account.entry.security-deposit/charges
 :<- [::security-deposit]
 (fn [{charges :charges} _]
   (map #(assoc % :url (charge-url %)) charges)))

(reg-sub
 :account.entry.security-deposit/checks
 :<- [::security-deposit]
 (fn [{checks :checks} _]
   checks))

(reg-sub
 :account.entry.security-deposit/loading?
 :<- [root-db-key]
 (fn [db _]
   (:loading db)))

(reg-sub
 :account.entry.security-deposit.check/showing-modal
 :<- [root-db-key]
 (fn [db _]
   (:showing-check-modal db)))

(reg-sub
 :account.entry.security-deposit.check/modal-data
 :<- [root-db-key]
 (fn [db _]
   (:check-modal-data db)))

(reg-sub
 :account.entry.security-deposit.check/statuses
 :<- [root-db-key]
 (fn [db _]
   (:check-statuses db)))
