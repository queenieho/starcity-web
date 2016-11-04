(ns admin.account.entry.security-deposit.subs
  (:require [admin.account.entry.security-deposit.db :as model :refer [root-db-key]]
            [re-frame.core :refer [reg-sub]]))

(reg-sub
 root-db-key
 (fn [db _]
   (model/security-deposit db)))

(reg-sub
 :account.entry.security-deposit/status
 :<- [root-db-key]
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
 :<- [root-db-key]
 (fn [{charges :charges} _]
   (map #(assoc % :url (charge-url %)) charges)))

(reg-sub
 :account.entry.security-deposit/loading?
 :<- [root-db-key]
 (fn [db _]
   (:loading db)))
