(ns starcity.models.rent
  (:require [blueprints.models.account :as account]
            [blueprints.models.customer :as customer]
            [blueprints.models.member-license :as member-license]
            [blueprints.models.rent-payment :as rent-payment]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [clojure.spec :as s]
            [datomic.api :as d]
            [ribbon.customer :as rcu]
            [starcity.config :as config :refer [config]]
            [toolbelt.async :refer [<!!?]]
            [toolbelt.datomic :as td]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Selectors
;; =============================================================================

;; NOTE: There will be some weird behavior here at renewal time. If the
;; currently active license ends next month, but member has chosen to renew,
;; what then?
(defn- due-date
  "Produces the due date of the next rent payment. Produces `nil` when the
  `member-license` will have expired by the next billing cycle."
  [member-license]
  (let [ends (c/to-date-time (member-license/ends member-license))
        next (t/plus (t/first-day-of-the-month (t/now)) (t/months 1) (t/days 4))]
    (when-not (t/after? next ends)
      (c/to-date next))))

(s/fdef scheduled-date
        :args (s/cat :member-license p/entity?)
        :ret (s/or :inst inst? :nothing nil?))


(defn next-payment
  [db account]
  (let [license (member-license/active db account)]
    {:amount (member-license/rate license)
     :due-by (due-date license)}))

(s/fdef next-payment
        :args (s/cat :db p/db? :account p/entity?)
        :ret (s/keys :req-un [::amount ::due-by]))


(defn bank-account
  "Produce the bank account data for this account."
  [db account]
  (when-let [customer (customer/by-account db account)]
    (when (customer/has-verified-bank-account? customer)
      (let [cus          (<!!? (rcu/fetch (config/stripe-private-key config)
                                          (customer/id customer)))
            bank-account (rcu/active-bank-account cus)]
        {:bank-name (rcu/account-name bank-account)
         :number    (rcu/account-last4 bank-account)}))))

(s/fdef bank-account
        :args (s/cat :db p/db? :account p/entity?)
        :ret (s/or :result (s/keys :req-un [::bank-name ::bank-number])
                   :nothing nil?))


;; =============================================================================
;; Queries
;; =============================================================================


(defn- query-payments [db account]
  (->> (d/q '[:find [?e ...]
              :in $ ?a
              :where
              [?a :account/license ?l]
              [?l :member-license/rent-payments ?e]]
            db (td/id account))
       (map (partial d/entity db))))

(defn payments
  "Query the 'history' of `account`'s rent payments. Pulls the last twelve
  payments sorted by most recent first."
  [db account]
  (let [payments (query-payments db account)]
    (->> (sort-by :rent-payment/period-start payments)
         (reverse))))

(s/fdef payments
        :args (s/cat :db p/db? :account p/entity?)
        :ret (s/* p/entity?))
