(ns starcity.api.admin.properties
  (:require [blueprints.models.account :as account]
            [blueprints.models.member-license :as member-license]
            [blueprints.models.property :as property]
            [blueprints.models.unit :as unit]
            [blueprints.models.license :as license]
            [clj-time.coerce :as c]
            [clojure.spec :as s]
            [compojure.core :refer [defroutes DELETE GET POST]]
            [datomic.api :as d]
            [ribbon.subscription :as rs]
            [ring.util.response :as response]
            [starcity.config :as config :refer [config]]
            [starcity.datomic :refer [conn]]
            [toolbelt.core :as tb]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Internal
;; =============================================================================

(def transit "application/transit+json")

;; =============================================================================
;; Overview

(defn clientize-property [db property]
  (let [now (java.util.Date.)]
    {:db/id                   (:db/id property)
     :property/name           (property/name property)
     :property/total-units    (count (property/units property))
     :property/total-occupied (count (property/occupied-units db property))
     :property/amount-total   (property/total-rent db property)
     :property/amount-due     (tb/round (property/amount-due db property now) 2)
     :property/amount-pending (tb/round (property/amount-pending db property now) 2)
     :property/amount-paid    (tb/round (property/amount-paid db property now) 2)}))

(defn overview [db]
  {:result
   (->> (d/q '[:find [?p ...] :where [?p :property/name _]] db)
        (map (comp (partial clientize-property db) (partial d/entity db))))})

;; =============================================================================
;; Entry

(defn- clientize-license-price [license-price]
  (-> (select-keys license-price [:db/id :license-price/price])
      (assoc :license-price/term (-> license-price :license-price/license :license/term))))

(defn- clientize-unit
  [db unit]
  (let [occupant (unit/occupied-by db unit)
        license  (when occupant (member-license/active db occupant))]
    (merge
     (select-keys unit [:db/id :unit/name])
     {:unit/account   (when occupant
                        {:db/id        (:db/id occupant)
                         :account/name (account/full-name occupant)})
      :unit/rate      (when license (member-license/rate license))
      :unit/term      (when license (member-license/term license))})))

(defn- units [db property]
  (->> (property/units property)
       (map (partial clientize-unit db))
       (sort-by :unit/name)))

(defn- licenses [property]
  (->> (:property/licenses property)
       (map clientize-license-price)
       (sort-by :license-price/term)))

(def dashboard-url "https://dashboard.stripe.com/")

(defn entry [db property-id]
  (let [property (d/entity db property-id)]
    {:result
     (merge
      (clientize-property db property)
      (select-keys property [:property/ops-fee :property/available-on])
      {:property/licenses   (licenses property)
       :property/units      (units db property)
       :property/stripe-url (str dashboard-url
                                 (property/managed-account-id property)
                                 "/payments")})}))

;; =============================================================================
;; Update

(defn- subscriptions [db property]
  (d/q '[:find [?sub-id ...]
         :in $ ?p
         :where
         [?l :member-license/subscription-id ?sub-id]
         [?l :member-license/unit ?u]
         [?l :member-license/status :member-license.status/active]
         [?p :property/units ?u]]
       db (:db/id property)))

(defn- update-ops-fee!
  "Whenever the `:property/ops-fee` value is updated, all subscriptions need to
  be updated with the new fee."
  [db property new-ops-fee]
  (doseq [sub-id (subscriptions db property)]
    (rs/update! (config/stripe-private-key config)
                sub-id
                :fee-percent new-ops-fee
                :managed-account (property/managed-account-id property))))

(s/fdef update-ops-fee!
        :args (s/cat :db p/db? :property p/entity? :ops-fee float?))

(defmulti update-tx (fn [e k v] k))

(defmethod update-tx :property/ops-fee [e _ new-fee]
  (let [fee (float new-fee)]
    (update-ops-fee! (d/db conn) (d/entity (d/db conn) e) fee)
    {:db/id e :property/ops-fee fee}))

(defmethod update-tx :property/available-on [e _ new-available-on]
  {:db/id e :property/available-on new-available-on})

(defmethod update-tx :property/licenses [e _ new-license-price]
  (-> (select-keys new-license-price [:db/id :license-price/price])
      (update :license-price/price float)))

(defn update! [conn property-id params]
  @(d/transact conn (reduce
                     (fn [acc [k v]]
                       (conj acc (update-tx property-id k v)))
                     []
                     params))
  {:result "ok"})

;; =============================================================================
;; Fetch Units

(defn fetch-units
  [db property-id {:keys [available-by license]}]
  (letfn [(-clientize [unit]
            (let [available (unit/available? db unit (or available-by (java.util.Date.)))]
              (-> (clientize-unit db unit)
                  (assoc :unit/available available
                         ;; NOTE: Using `:unit/market` to avoid disambiguation
                         ;; with the `:unit/rate` produced by `clientize-unit`.
                         ;; Obviously, finding a way to unify these things into
                         ;; one function would be better.
                         :unit/market (when-some [x license] (unit/rate unit (d/entity db x)))))))]
    {:result (->> (property/units (d/entity db property-id))
                  (map -clientize)
                  (sort-by :unit/name))}))

(s/def :fetch-units/available-by inst?)
(s/def :fetch-units/license integer?)
(s/def :fetch-units/result (s/* map?))
(s/fdef fetch-units
        :args (s/cat :db p/db?
                     :property-id integer?
                     :opts (s/keys :opt-un [:fetch-units/available-by
                                            :fetch-units/license]))
        :ret (s/keys :req-un [:fetch-units/result]))

;; =============================================================================
;; Unit Entry

(defn- clientize-unit-entry [db unit]
  (let [occupant (unit/occupied-by db unit)]
    (merge
     (select-keys unit [:db/id :unit/name])
     {:property/licenses (->> (unit/property unit)
                              :property/licenses
                              (map clientize-license-price))
      :unit/licenses     (map clientize-license-price (:unit/licenses unit))
      :unit/account      (when occupant
                           {:db/id        (:db/id occupant)
                            :account/name (account/full-name occupant)})})))

(defn unit-entry
  [conn unit-id]
  (let [unit (d/entity (d/db conn) unit-id)]
    {:result (clientize-unit-entry (d/db conn) unit)}))

;; =============================================================================
;; Update License Price

(defn create-unit-license-price
  [conn unit-id price term]
  (assert (and (number? price) (> price 0)))
  (let [license (license/by-term (d/db conn) term)]
    @(d/transact conn [{:db/id         unit-id
                        :unit/licenses {:license-price/license (:db/id license)
                                        :license-price/price   (float price)}}])
    {:result "Ok."}))

(defn update-license-price
  [conn license-price-id price]
  (assert (and (number? price) (> price 0)))
  @(d/transact conn [{:db/id               license-price-id
                      :license-price/price (float price)}])
  {:result "Ok."})

;; =============================================================================
;; Routes
;; =============================================================================

(defroutes routes
  (GET "/overview" []
       (fn [_]
         (-> (overview (d/db conn))
             (response/response)
             (response/content-type transit))))

  (GET "/:property-id" [property-id]
       (fn [_]
         (-> (entry (d/db conn) (tb/str->int property-id))
             (response/response)
             (response/content-type transit))))

  (POST "/:property-id" [property-id]
        (fn [{:keys [body-params] :as req}]
          (-> (update! conn (tb/str->int property-id) body-params)
              (response/response)
              (response/content-type transit))))

  ;; =====================================
  ;; Units

  (GET "/:property-id/units" [property-id]
       (fn [{params :params}]
         (let [params (tb/transform-when-key-exists params
                        {:available-by (comp c/to-date c/from-long tb/str->int)
                         :license      tb/str->int})]
           (-> (fetch-units (d/db conn) (tb/str->int property-id) params)
               (response/response)
               (response/content-type transit)))))

  (GET "/:property-id/units/:unit-id" [unit-id]
       (fn [_]
         (-> (unit-entry conn (tb/str->int unit-id))
             (response/response)
             (response/content-type transit))))

  (POST "/:property-id/units/:unit-id/license-prices" [unit-id]
        (fn [{params :params}]
          (-> (create-unit-license-price conn
                                         (tb/str->int unit-id)
                                         (:price params)
                                         (:term params))
              (response/response)
              (response/content-type transit))))

  (POST "/:property-id/units/:unit-id/license-prices/:price-id" [price-id]
        (fn [{params :params}]
          (-> (update-license-price conn (tb/str->int price-id) (:price params))
              (response/response)
              (response/content-type transit))))

  (DELETE "/:property-id/units/:unit-id/license-prices/:price-id" [price-id]
          (fn [_]
            @(d/transact conn [[:db.fn/retractEntity (tb/str->int price-id)]])
            (-> (response/response {:result "Ok."})
                (response/content-type transit)))) )
