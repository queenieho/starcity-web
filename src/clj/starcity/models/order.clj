(ns starcity.models.order
  (:refer-clojure :exclude [update])
  (:require [blueprints.models
             [account :as account]
             [charge :as charge]
             [customer :as customer]]
            [clj-time.core :as t]
            [clojure
             [spec :as s]
             [string :as string]]
            [datomic.api :as d]
            [plumbing.core :as plumbing]
            [ribbon
             [charge :as rc]
             [customer :as rcu]
             [plan :as rp]
             [subscription :as rs]]
            [starcity
             [config :as config :refer [config]]
             [datomic :refer [tempid]]]
            [starcity.models
             [service :as service]]
            [toolbelt
             [async :refer [<!!?]]
             [date :as date]
             [predicates :as p]]
            [taoensso.timbre :as timbre]
            [clojure.core.async :as a]))

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
;; Orders Playground: 6/15/17
;; =============================================================================


;; To place the order, we need to know the following:
;; 1. Billed once or on a subscription?
;; 2. Description of order (code +? variant)
;; 3. Price

(defn- customer-id [db account]
  (:stripe-customer/customer-id (customer/by-account db account)))


(defn- stripe-desc [account order]
  (let [email (:account/email account)
        quant (or (:order/quantity order) 1)
        code  (get-in order [:order/service :service/code])
        vart  (get-in order [:order/variant :svc-variant/name])]
    (if (some? vart)
      (format "%s : x%s : %s (%s)" email quant code vart)
      (format "%s : x%s : %s" email quant code))))


(defn- credit-card [customer-id]
  (let [customer (rcu/fetch (config/stripe-private-key config) customer-id)
        card     (rcu/active-credit-card (<!!? customer))]
    (if (nil? card)
      (throw (ex-info "Cannot place order; customer has no credit card!"
                      {:customer customer-id}))
      card)))


(defmulti place-order!*
  (fn [_ _ order _]
    (get-in order [:order/service :service/billed])))


(defmethod place-order!* :default [_ account order _]
  (throw (ex-info "This order has an unknown bill method; cannot place!"
                  {:order   (:db/id order)
                   :account (:account/email account)})))


(defn- make-charge! [db account order price opts]
  (let [cus   (customer-id db account)
        card  (rcu/token (credit-card cus))
        desc  (str (stripe-desc account order)
                   (if-some [s (:desc opts)] (str " : \"" s "\"") ""))
        price (int (* 100 price))]
    (rc/create! (config/stripe-private-key config) price card
                :customer-id cus
                :description desc
                :email (account/email account))))


(defmethod place-order!* :service.billed/once
  [conn account order {:keys [price] :as opts}]
  (let [price  (* (or price (computed-price order))
                  (or (quantity order) 1))
        charge (<!!? (make-charge! (d/db conn) account order price opts))]
    @(d/transact conn [{:db/id         (:db/id order)
                        :order/ordered (java.util.Date.)
                        :order/price   price
                        :stripe/charge (charge/create (:id charge) price
                                                      :purpose (stripe-desc account order)
                                                      :account account)}])))


(defn- get-plan! [order price]
  (let [key       (config/stripe-private-key config)
        price     (int (* 100 price))
        plan-name (get-in order [:order/service :service/code])
        p-remote  (a/<!! (rp/fetch key plan-name))]
    (if (p/throwable? p-remote)
      (<!!? (rp/create! key plan-name plan-name price :month))
      p-remote)))


(defn- make-subscription! [db account order price]
  (let [plan (get-plan! order price)
        cus  (customer-id db account)
        sub  (<!!? (rs/create! (config/stripe-private-key config) cus (:id plan)
                               :quantity (int (or (quantity order) 1))))]
    [plan sub]))


(defmethod place-order!* :service.billed/monthly
  [conn account order {:keys [price] :as opts}]
  (assert (or (some? price) (some? (get-in order [:order/service :service/price])))
          "Services subscribed to must have a price!")
  (let [price      (or price (get-in order [:order/service :service/price]))
        [plan sub] (make-subscription! (d/db conn) account order price)]
    @(d/transact conn [{:db/id          (:db/id order)
                        :order/ordered  (java.util.Date.)
                        :stripe/plan-id (:id plan)
                        :stripe/subs-id (:id sub)}])))


(defn place-order!
  [conn account order & {:as opts}]
  (assert (some? (or (:price opts) (computed-price order)))
          "Order cannot be placed without a price!")
  (assert (not (ordered? order))
          "Order has already been ordered!")
  (place-order!* conn account order opts))

(s/def ::price float?)
(s/def ::desc string?)
(s/fdef place-order!
        :args (s/cat :conn p/conn?
                     :account p/entity?
                     :order p/entity?
                     :opts (s/keys* :opt-un [::price ::desc])))

(comment
  (def conn starcity.datomic/conn)

  (def account
    (d/entity (d/db conn) [:account/email ""]))

  (d/q '[:find (pull ?o [:db/id
                         :order/price
                         :order/quantity
                         :stripe/subs-id
                         {:order/variant [:db/id
                                          :svc-variant/name
                                          :svc-variant/price]}
                         {:order/service [:db/id
                                          :service/code
                                          :service/price]}])
         :in $ ?a
         :where
         [?o :order/account ?a]]
       (d/db conn) (:db/id account))


  (let [service (service/by-code (d/db conn) "plants,planter")
        order   (by-account (d/db conn) account service)]
    (place-order! conn account order))


  (let [cus (customer-id (d/db conn) account)]
    (<!!? (rcu/update! (config/stripe-private-key config) cus
                       :default-source (rcu/token (credit-card cus)))))



  )

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
