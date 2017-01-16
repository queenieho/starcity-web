(ns starcity.models.charge
  (:require [datomic.api :as d]
            [starcity.datomic :refer [conn]]
            [starcity.datomic.partition :refer [tempid]]
            [starcity.spec]
            [clojure.spec :as s]
            [plumbing.core :refer [assoc-when]]
            [starcity.util :refer :all]))

;; =============================================================================
;; Transactions

(defn- status-tx [status charge]
  {:db/id         (:db/id charge)
   :charge/status status})

(def succeeded-tx
  "The transaction to mark `charge` as succeeded."
  (partial status-tx :charge.status/succeeded))

(def failed-tx
  "The transaction to mark `charge` as failed."
  (partial status-tx :charge.status/failed))

(defn succeeded
  "Mark `charge` as succeeded."
  [charge]
  (d/transact conn [(succeeded-tx charge)]))

(defn failed
  "Mark `charge` as failed."
  [charge]
  (d/transact conn [(failed-tx charge)]))

;; =============================================================================
;; Lookup

(defn lookup
  "Look up a charge by the external `charge-id`."
  [charge-id]
  (d/entity (d/db conn) [:charge/stripe-id charge-id]))

;; =============================================================================
;; Predicates

(defn- is-status? [status charge]
  (= (:charge/status charge) status))

(def is-succeeded? (partial is-status? :charge.status/succeeded))
(def is-failed? (partial is-status? :charge.status/failed))
(def is-pending? (partial is-status? :charge.status/pending))

(defn is-rent-ach-charge?
  "Returns `true` if `charge` is part of a rent payment."
  [conn charge]
  (d/q '[:find ?e .
         :in $ ?c
         :where
         [?e :rent-payment/charge ?c]
         [?e :rent-payment/method :rent-payment.method/ach]]
       (d/db conn) (:db/id charge)))

(defn is-security-deposit-charge?
  "Returns `true` if `charge` is part of a security deposit."
  [conn charge]
  (d/q '[:find ?security-deposit .
         :in $ ?charge
         :where
         [?security-deposit :security-deposit/charges ?charge]]
       (d/db conn) (:db/id charge)))

;; =============================================================================
;; Selectors

(s/def ::status #{:charge.status/succeeded
                  :charge.status/failed
                  :charge.status/pending})

(def account :charge/account)

;; =============================================================================
;; Transactions

(defn create
  [stripe-id account & {:keys [purpose status]
                        :or   {status :charge.status/pending}}]
  (assoc-when
   {:db/id            (tempid)
    :charge/stripe-id stripe-id
    :charge/account   (:db/id account)
    :charge/status    status}
   :charge/purpose purpose))

(s/def ::purpose string?)
(s/fdef create
        :args (s/cat :stripe-id string?
                     :account entity?
                     :opts (s/keys* :opt-un [::purpose ::status]))
        :ret (s/keys :req [:db/id :charge/stripe-id :charge/account :charge/status]
                     :opt [:charge/purpose]))
