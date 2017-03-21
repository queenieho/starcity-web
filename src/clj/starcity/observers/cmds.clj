(ns starcity.observers.cmds
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [datomic.api :as d]
            [plumbing.core :as plumbing]
            [starcity.models
             [cmd :as cmd]
             [msg :as msg]
             [rent-payment :as rent-payment]]
            [starcity.models.stripe.customer :as customer]
            [starcity.observers.cmds.stripe :as stripe]
            [starcity.services.stripe.customer :as customer-service]
            [taoensso.timbre :as timbre]
            [starcity.models.account :as account]))

;; =============================================================================
;; Global
;; =============================================================================

(defmulti handle (fn [conn cmd] (:cmd/key cmd)))

(defmethod handle :default
  [_ cmd]
  (timbre/debug ::no-handler cmd))

;; =============================================================================
;; Accounts
;; =============================================================================

(defmethod handle cmd/create-account-key
  [conn {{:keys [email password first-name last-name]} :cmd/params :as cmd}]
  @(d/transact conn [(account/create email password first-name last-name)
                     (msg/account-created email)
                     (cmd/successful cmd)]))

;; =============================================================================
;; Rent
;; =============================================================================

;; Autopay payments are created via webhook notification from Stripe.
(defn- active-licenses
  "Query all active licenses that are not on autopay."
  [conn]
  (d/q '[:find ?e ?p
         :where
         ;; active licenses
         [?e :member-license/status :member-license.status/active]
         [?e :member-license/price ?p]
         ;; not on autopay
         [(missing? $ ?e :member-license/subscription-id)]]
       (d/db conn)))

(defn- period-end [t]
  (-> t c/to-date-time t/last-day-of-the-month c/to-date))

(defn- create-payments [start licenses]
  (mapv
   (fn [[e amount]]
     (let [p (rent-payment/create amount start (period-end start) :rent-payment.status/due)]
       {:db/id                        e
        :member-license/rent-payments p}))
   licenses))

;; Triggered monthly by scheduler. Creates rent payments at the beginning of the
;; month.
(defmethod handle cmd/create-rent-payments-key
  [conn {time-period :cmd/params :as cmd}]
  (try
    (let [txes (->> (active-licenses conn) (create-payments time-period))]
      @(d/transact conn (conj txes
                              (cmd/successful cmd)
                              (msg/rent-payments-created (map :db/id txes)))))
    (catch Throwable t
      @(d/transact conn [(cmd/failed cmd)])
      (throw t))))

;; =============================================================================
;; Stripe
;; =============================================================================

(defmethod handle cmd/stripe-webhook-event-key
  [conn cmd]
  (try
    ;; The Stripe event will dispatch on its own event type.
    (d/transact conn (conj (stripe/handle conn cmd)
                           (cmd/successful cmd)))
    (catch Throwable t
      @(d/transact conn [(cmd/failed cmd)])
      (timbre/error t ::handle cmd)
      (throw t))))

(defmethod handle cmd/delete-customer-key
  [conn {stripe-customer-id :cmd/params :as cmd}]
  (try
    (let [stripe-customer (d/entity (d/db conn) stripe-customer-id)]
      ;; NOTE: This is synchronous right now, so this is ok -- when it becomes
      ;; async, well, you know.
      (customer-service/delete! (customer/id stripe-customer))
      @(d/transact conn [(cmd/successful cmd)
                         [:db.fn/retractEntity stripe-customer-id]]))
    (catch Throwable t
      @(d/transact conn [(cmd/failed cmd)])
      (throw t))))

;; =============================================================================
;; Session
;; =============================================================================

(defn- get-session-id [conn account-id]
  (d/q '[:find ?s .
         :in $ ?a
         :where
         [?s :session/account ?a]]
       (d/db conn) account-id))

(defn- delete-session [conn account-id]
  (when-let [session-id (get-session-id conn account-id)]
    [:db.fn/retractEntity session-id]))


(defmethod handle cmd/delete-session-key
  [conn {account-id :cmd/params :as cmd}]
  @(d/transact conn (plumbing/conj-when
                     [(cmd/successful cmd)]
                     (delete-session conn account-id))))
