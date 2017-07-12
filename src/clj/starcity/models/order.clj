(ns starcity.models.order
  (:refer-clojure :exclude [update])
  (:require [blueprints.models.account :as account]
            [blueprints.models.charge :as charge]
            [blueprints.models.service :as service]
            [clj-time.core :as t]
            [clojure.spec :as s]
            [clojure.string :as string]
            [datomic.api :as d]
            [plumbing.core :as plumbing]
            [starcity.datomic :refer [tempid]]
            [toolbelt.date :as date]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Selectors
;; =============================================================================

(defn price
  "The price of this `order`."
  [order]
  (:order/price order))

(s/fdef price
        :args (s/cat :order p/entity?)
        :ret (s/or :nothing nil? :price float?))


(defn computed-price
  "The price of this `order`, taking into consideration possible variants and
  the price of the service."
  [order]
  (or (:order/price order)
      (-> order :order/variant :svc-variant/price)
      (service/price (:order/service order))))

(s/fdef computed-price
        :args (s/cat :order p/entity?)
        :ret (s/or :nothing nil? :price float?))


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


(def ordered-at
  "Instant at which the order was placed."
  :order/ordered)

(s/fdef ordered-at
        :args (s/cat :order p/entity?)
        :ret (s/or :inst inst? :nothing nil?))


(defn computed-name
  [order]
  (let [service (:order/service order)]
    (if-let [vn (-> order :order/variant :svc-variant/name)]
      (str (service/name service) " - " (string/capitalize vn))
      (service/name service))))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn ordered?
  "An order is considered /ordered/ when it has both an order placement time
  AND a subscription or non-failed charge."
  [order]
  (boolean
   (and (some? (ordered-at order))
        (or (:stripe/subs-id order)
            (when-let [c (:stripe/charge order)]
              (not= (:charge/status c) :charge.status/failed))))))

(s/fdef ordered?
        :args (s/cat :order p/entity?)
        :ret boolean?)


(comment
  (ordered? {:order/ordered  (java.util.Date.)
             ;; :stripe/subs-id "sub"
             :stripe/charge  {:charge/status :charge.status/pending}
             })

  )

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

(defn orders
  "All of `account`'s orders."
  [db account]
  (->> (d/q '[:find [?o ...]
              :in $ ?a
              :where
              [?o :order/account ?a]]
            db (:db/id account))
       (map (partial d/entity db))))

;; =============================================================================
;; Transactions
;; =============================================================================


(s/def ::quantity (s/and pos? float?))
(s/def ::desc string?)
(s/def ::variant integer?)
(s/def ::opts (s/keys :opt-un [::quantity ::desc ::variant]))

(defn create
  ([account service]
   (create account service {}))
  ([account service {:keys [quantity desc variant] :as opts}]
   (plumbing/assoc-when
    {:db/id         (tempid)
     :order/uuid    (d/squuid)
     :order/service (:db/id service)
     :order/account (:db/id account)}
    :order/variant variant
    :order/quantity quantity
    :order/desc desc)))

(s/fdef create
        :args (s/cat :account p/entity?

                     :service p/entity?
                     :opts    (s/? ::opts))
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


;; =============================================================================
;; Clientize
;; =============================================================================

(defn- variant-name [order]
  (-> order :order/variant :svc-variant/name))

(defn clientize
  [order]
  (let [service (:order/service order)
        desc    (if (string/blank? (desc order)) (service/desc service) (desc order))
        name    (if-let [vn (variant-name order)]
                  (str (service/name service) " - " (string/capitalize vn))
                  (service/name service))]
    {:id       (:db/id order)
     :name     name
     :desc     desc
     :price    (computed-price order)
     :rental   (service/rental service)
     :quantity (quantity order)
     :billed   (-> service :service/billed clojure.core/name keyword)}))


;; =============================================================================
;; Order Analytics: 6/29/17
;; =============================================================================

;; Date of request (if available), date request completed (if available),
;; associated member, $ charge (initiated / completed / success) would be a good
;; starting place.

(defn order-report [db]
  (let [qr (d/q '[:find ?e ?email ?date-of-request
                  :where
                  [?e :order/account ?a ?dortx]
                  [?dortx :db/txInstant ?date-of-request]
                  [?e :order/service ?s]
                  [?a :account/email ?email]]
                db)]
    (map
     (fn [[e email date-of-request]]
       (let [ent     (d/entity db e)
             price   (if-some [p (computed-price ent)] (str "$" p) "N/A")
             tz      (t/time-zone-for-id "America/Los_Angeles")
             dor     (-> date-of-request (date/to-utc-corrected-date tz) date/short-date)
             ordered (when-some [d (ordered-at ent)] (-> d (date/to-utc-corrected-date tz) date/short-date))]
         ;; amount charged, service, member, date requested, date charged
         [price (computed-name ent) email dor (or ordered "N/A")]))
     qr)))


(comment
  (->> (order-report (d/db starcity.datomic/conn))
       (map (comp (partial apply str) (partial interpose ",")))
       (interpose "\n")
       (apply str))

  )
