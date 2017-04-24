(ns starcity.models.order
  (:refer-clojure :exclude [update])
  (:require [datomic.api :as d]
            [starcity.datomic :refer [tempid]]
            [clojure.spec :as s]
            [toolbelt.predicates :as p]
            [plumbing.core :as plumbing]
            [taoensso.timbre :as timbre]))

;; =============================================================================
;; Selectors
;; =============================================================================

(defn quantity
  "The number of `service` ordered."
  [order]
  (:order/quantity order))

(s/fdef quantity
        :args (s/cat :order p/entity?)
        :ret (s/or :nothing nil? :quantity pos-int?))

(def desc
  "The description of the order."
  :order/desc)

(s/fdef desc
        :args (s/cat :order p/entity?)
        :ret (s/or :nothing nil? :desc string?))

(def variant
  "The variant of the service chosen with this order."
  :order/variant)

(s/fdef desc
        :args (s/cat :order p/entity?)
        :ret (s/or :nothing nil? :variant p/entity?))

;; =============================================================================
;; Queries
;; =============================================================================

(defn by-account
  "Find an order given the `account` and `service`."
  [db account service]
  (->> (d/q '[:find ?o .
              :in $ ?a ?s
              :where
              [?o :order/account ?a]
              [?o :order/service ?s]]
            db (:db/id account) (:db/id service))
       (d/entity db)))

(s/fdef by-account
        :args (s/cat :db p/db?
                     :account p/entity?
                     :service p/entity?)
        :ret (s/or :entity p/entity? :nothing nil?))

(def exists?
  "Does `account` have an order for `service`?"
  (comp p/entity? by-account))

;; =============================================================================
;; Transactions
;; =============================================================================

(s/def ::quantity (s/and pos? float?))
(s/def ::desc string?)
(s/def ::variant integer?)
(s/def ::opts (s/keys :opt-un [::quantity ::desc ::variant]))

(defn create
  [account service {:keys [quantity desc variant] :as opts}]
  (plumbing/assoc-when
   {:db/id         (tempid)
    :order/service (:db/id service)
    :order/account (:db/id account)}
   :order/variant variant
   :order/quantity quantity
   :order/desc desc))

(s/fdef create
        :args (s/cat :account p/entity?

                     :service p/entity?
                     :opts    ::opts)
        :ret map?)

(defn update
  [order {:keys [quantity desc variant]}]
  (plumbing/assoc-when
   {:db/id (:db/id order)}
   :order/quantity quantity
   :order/desc desc
   :order/variant variant))

(s/fdef update
        :args (s/cat :order p/entity?
                     :opts  ::opts)
        :ret map?)

(defn remove-existing
  [db account service]
  (when-let [order (by-account db account service)]
    [:db/retractEntity (:db/id order)]))

(s/fdef remove-existing
        :args (s/cat :db p/db? :account p/entity? :service p/entity?))
