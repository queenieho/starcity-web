(ns starcity.events.security-deposit
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [starcity
             [datomic :refer [conn]]
             [util :refer :all]]
            [starcity.events.plumbing :refer [defproducer]]
            [starcity.models
             [account :as account]
             [charge :as charge]
             [member-license :as member-license]
             [security-deposit :as security-deposit]]
            [starcity.models.stripe.customer :as customer]
            [starcity.services.stripe :as stripe]))

;; =============================================================================
;; Pay Security Deposit Remainder by ACH

(defn- charge-amount [deposit]
  (- (security-deposit/amount-required deposit)
     (security-deposit/amount-received deposit)))

(defn- create-stripe-charge!
  "Create the Charge on Stripe."
  [conn account deposit]
  (let [customer (account/stripe-customer account)
        license  (member-license/active conn account)]
    (cond
      (nil? customer)
      (throw (ex-info "No linked customer." {:account (:db/id account)}))

      (not (customer/has-verified-bank-account? customer))
      (throw (ex-info "No bank account linked." {:account  (:db/id account)
                                                 :customer (customer/id customer)}))

      (security-deposit/paid-in-full? deposit)
      (throw (ex-info "Security deposit already paid." {:account  (:db/id account)
                                                        :customer (customer/id customer)
                                                        :deposit  (:db/id deposit)}))

      :otherwise (-> (stripe/charge (int (* 100 (charge-amount deposit)))
                                    (customer/bank-account-token customer)
                                    (account/email account)
                                    :customer-id (customer/id customer)
                                    :managed-account (member-license/managed-account-id license))
                     (get-in [:body :id])))))

(defn- update-db [conn charge-id deposit]
  (let [total  (security-deposit/amount-required deposit)
        charge (charge/create charge-id (security-deposit/account deposit))]
    @(d/transact conn [charge
                       {:db/id                            (:db/id deposit)
                        :security-deposit/charges         (:db/id charge)
                        :security-deposit/amount-received total}])))

(defproducer pay-remainder! ::pay-remainder-ach [deposit]
  (let [account   (security-deposit/account deposit)
        charge-id (create-stripe-charge! conn account deposit)]
    {:result    (update-db conn charge-id deposit)
     :deposit   deposit
     :account   account
     :charge-id charge-id}))

(s/fdef pay-remainder!
        :args (s/cat :deposit entity?)
        :ret chan?)

(comment
  (def account (account/by-email "member@test.com"))

  (pay-remainder! (security-deposit/by-account account))

  )
