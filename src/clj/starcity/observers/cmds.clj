(ns starcity.observers.cmds
  (:require [clojure.string :as string]
            [datomic.api :as d]
            [plumbing.core :as plumbing]
            [starcity.models
             [account :as account]
             [address :as address]
             [application :as application]
             [cmd :as cmd]
             [msg :as msg]
             [note :as note]
             [property :as property]
             [rent-payment :as rent-payment]]
            [starcity.observers.cmds.stripe :as stripe]
            [starcity.services
             [community-safety :as community-safety]
             [weebly :as weebly]]
            [starcity.services.stripe.customer :as customer-service]
            [starcity.util.async :refer [<!!?]]
            [taoensso.timbre :as timbre]
            [toolbelt.date :as date]))

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
;; Application
;; =============================================================================

(defn- background-check! [account]
  (let [address     (-> account account/member-application application/address)
        middle-name (account/middle-name account)]
    (community-safety/background-check (:db/id account)
                                       (account/first-name account)
                                       (account/last-name account)
                                       (account/email account)
                                       (account/dob account)
                                       (plumbing/assoc-when
                                        {:address     {:city        (address/city address)
                                                       :state       (address/state address)
                                                       :postal-code (address/postal-code address)}}
                                        :middle-name (if (string/blank? middle-name) nil middle-name)))))

(defmethod handle :application/submit
  [conn {{id :application-id} :cmd/data :as cmd}]
  (try
    (let [application (d/entity (d/db conn) id)
          account     (application/account application)
          check       (background-check! account)]
      (timbre/info :application.submit/community-safety {:application id
                                                         :account     (account/email account)})
      @(d/transact conn [{:db/id                       (d/tempid :db.part/starcity)
                          :community-safety/account    (:db/id account)
                          :community-safety/report-url (community-safety/report-url check)}
                         (cmd/successful cmd)]))
    (catch Throwable t
      (timbre/error t :application.submit/community-safety {:application id})
      @(d/transact conn [(cmd/failed cmd)]))))

;; =============================================================================
;; Collaborators
;; =============================================================================

(defn- collaborator-tx
  "If a collaborator account identified by `email` exists, add `note` to it;
  otherwise create a collaborator account and and `note` to it."
  [conn email note]
  (-> (if-let [account (account/by-email (d/db conn) email)]
        [{:db/id         (:db/id account)
          :account/notes note}]
        [(assoc (account/collaborator email) :account/notes note)])
      (conj (msg/note-created note true))))

(defmethod handle cmd/add-collaborator-key
  [conn {{:keys [email type message]} :cmd/params :as cmd}]
  (let [subject (format "Collaboration request from: %s, %s" email type)
        note    (note/create subject message)]
    @(d/transact conn (conj (collaborator-tx conn email note)
                            (cmd/successful cmd)))))

;; =============================================================================
;; Newsletter
;; =============================================================================

(defmethod handle cmd/subscribe-to-newsletter-key
  [conn {{email :email} :cmd/params :as cmd}]
  (try
    (let [res (<!!? (weebly/add-subscriber! email))]
      (timbre/info cmd/subscribe-to-newsletter-key {:email email
                                                    :uuid  (:cmd/uuid cmd)})
      @(d/transact conn [(cmd/successful cmd)]))
    (catch Throwable t
      (timbre/error t cmd/subscribe-to-newsletter-key {:email email
                                                       :uuid  (:cmd/uuid cmd)})
      @(d/transact conn [(cmd/failed cmd)]))))

;; =============================================================================
;; Rent
;; =============================================================================

(defn- active-licenses
  "Query all active licenses that are not on autopay."
  [db]
  (d/q '[:find ?l ?pr ?p
         :where
         ;; active licenses
         [?l :member-license/status :member-license.status/active]
         [?l :member-license/unit ?u]
         [?pr :property/units ?u]
         [?l :member-license/price ?p]
         [?l :member-license/commencement ?c]
         ;; not on autopay
         [(missing? $ ?l :member-license/subscription-id)]
         ;; license has commenced
         [(.before ^java.util.Date ?c ?now)]]
       db))


(defn- create-payments [db start query-result]
  (mapv
   (fn [[member-license-id property-id amount]]
     (let [tz    (property/time-zone (d/entity db property-id))
           start (date/beginning-of-day start tz)
           end   (date/end-of-month start tz)
           py    (rent-payment/create amount start end :rent-payment.status/due)]
       (timbre/debug "create payments" {:tz    tz
                                        :start start
                                        :end   end})
       {:db/id                        member-license-id
        :member-license/rent-payments py}))
   query-result))


;; Triggered monthly by scheduler. Creates rent payments at the beginning of the
;; month.
(defmethod handle cmd/create-rent-payments-key
  [conn {time-period :cmd/params :as cmd}]
  (try
    (let [db   (d/db conn)
          txes (create-payments db time-period (active-licenses db))]
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
      (customer-service/delete! (:stripe-customer/customer-id stripe-customer))
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
