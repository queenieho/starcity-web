(ns starcity.models.autopay
  (:require [blueprints.models.account :as account]
            [blueprints.models.customer :as customer]
            [blueprints.models.member-license :as member-license]
            [blueprints.models.property :as property]
            [clj-time.coerce :as c]
            [clj-time.core :as t]
            [clojure.spec :as s]
            [datomic.api :as d]
            [ribbon.connect :as rcn]
            [ribbon.customer :as rcu]
            [ribbon.plan :as rp]
            [ribbon.subscription :as rs]
            [starcity.config :as config :refer [config]]
            [starcity.datomic :refer [conn]]
            [taoensso.timbre :as timbre]
            [toolbelt.async :refer [<!!?]]
            [toolbelt.core :as tb]
            [toolbelt.datomic :as td]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Setup Status
;; =============================================================================

(s/def ::autopay-status #{"error" "none" "bank-needed" "unverified" "verified"})


(defn- fetch-setup-status [db account]
  (try
    (let [cus (<!!? (rcu/fetch (config/stripe-private-key config)
                               (customer/id (customer/by-account db account))))]
      (cond
        (not (rcu/has-bank-account? cus))    "init"
        (rcu/has-verified-bank-account? cus) "verified"
        :otherwise                           "unverified"))
    (catch Exception e
      (timbre/error e ::fetch-customer {:account (account/email account)})
      "error")))

(defn setup-status
  "Produce the status of autopay setup for `account`."
  [db account]
  (if-not (customer/by-account db account)
    "none"
    (fetch-setup-status db account)))

(s/fdef setup-status
        :args (s/cat :db p/db? :account p/entity?)
        :ret ::autopay-status)

;; =============================================================================
;; Subscribed
;; =============================================================================


(defn subscribed?
  "Is `account` subscribed to autopay?"
  [db account]
  (let [ml (member-license/active db account)]
    (member-license/autopay-on? ml)))


;; =============================================================================
;; Setup Details
;; =============================================================================


(defn- is-first-day-of-month? [date]
  (= (t/day (c/to-date-time date)) 1))

(s/fdef is-first-day-of-month?
        :args (s/cat :date-time inst?)
        :ret boolean?)


(defn- is-past? [date]
  (t/before? (c/to-date-time date) (t/now)))


(defn- first-day-next-month [date]
  (t/plus (t/first-day-of-the-month (c/to-date-time date))
          (t/months 1)))


(defn- plan-start-date*
  [member-license]
  (let [commencement (member-license/commencement member-license)]
    ;; If the commencement date is already the first day of the month, leave it
    ;; alone.
    (cond
      ;; The commencement date already passed, so the subscription needs to
      ;; start in the next calendar month. It's assumend that rent up until that
      ;; point is being collected with some other means.
      (is-past? commencement)               (first-day-next-month (java.util.Date.))
      ;; If commencement is not in the past and is already on the first day of
      ;; the month, it's also the plan start date.
      (is-first-day-of-month? commencement) commencement
      ;; Otherwise, it's the first day of the month following commencement.
      :otherwise (first-day-next-month commencement))))


(def ^:private plan-start-date
  "The plan start date is the first day of the month following the
  `member-license`'s commencement date, assuming the commencement date is still
  in the future. Otherwise, it's the beginning of the next calendar month."
  (comp c/to-date plan-start-date*))

(s/fdef plan-start-date
        :args (s/cat :member-license p/entity?)
        :ret inst?)


(defn- prorated-amount
  "Calculate the rent that's due upon move-in.

  (RATE / DAYS-IN-MONTH) * DAYS"
  [member-license]
  (let [commencement (c/to-date-time (member-license/commencement member-license))
        start-date   (plan-start-date* member-license)]
    ;; if commencement is already past, regard rent as paid.
    (if (is-past? commencement)
      0
      (->> (* (float (/ (member-license/rate member-license)
                        (t/number-of-days-in-the-month commencement)))
              (t/in-days (t/interval commencement start-date)))
           (tb/round 2)))))

(s/fdef prorated-amount
        :args (s/cat :member-license p/entity?)
        :ret float?)

(s/def ::rent-amount float?)
(s/def ::commencement inst?)
(s/def ::plan-start inst?)
(s/def ::term integer?)
(s/def ::amount-due float?)

(defn- plan-details
  "Retrieve the details of a member's license that are relevant to setting up
  autopay."
  [license]
  {:rent-amount  (member-license/rate license)
   :commencement (member-license/commencement license)
   :end-date     (member-license/ends license)
   :term         (member-license/term license)
   :plan-start   (plan-start-date license)
   :amount-due   (prorated-amount license)})

(s/fdef plan-details
        :args (s/cat :license p/entity?)
        :ret (s/keys :req-un [::rent-amount ::commencement ::term ::plan-start ::amount-due]))


(defn setup
  "Retrieve the setup data for `account` iff `account` should be 'setup'."
  [db account]
  (let [license (member-license/active db account)]
    (when-not (subscribed? db account)
      {:status (setup-status db account)
       :plan   (plan-details license)})))

(s/fdef setup
        :args (s/cat :db p/db? :account p/entity?)
        :ret (s/or :nothing nil? :setup (s/keys :req-un [::status ::plan])))


;; =============================================================================
;; Subscribe!
;; =============================================================================


(defn- plan-id [member-license]
  (str (:db/id member-license)))


(defn- plan-name [account property]
  (str (account/full-name account) "'s rent at " (property/name property)))


(defn- plan-amount [member-license]
  (int (* 100 (member-license/rate member-license))))


(defn- create-customer!* [db account license customer]
  (let [stripe      (config/stripe-private-key config)
        managed     (member-license/managed-account-id license)
        {token :id} (<!!? (rcn/create-bank-token! stripe
                                                  (customer/id customer)
                                                  (customer/bank-token customer)
                                                  managed))
        ccus        (<!!? (rcu/create! stripe (account/email account) token
                                       :managed-account managed))]
    (customer/create (:id ccus) account
                     :bank-token (:id (rcu/active-bank-account ccus))
                     :managing-property (member-license/property license))))


(defn- create-customer! [db account license]
  (or (member-license/customer license)
      (let [customer (customer/by-account db account)]
        (assert (some? customer) "A platform customer must be present!")
        (assert (some? (customer/bank-token customer))
                "The platform customer must have a linked bank account!")
        (create-customer!* db account license customer))))

(s/fdef create-customer!
        :args (s/cat :db p/db? :account p/entity? :license p/entity?)
        :ret p/entity?)


(defn- maybe-existing-plan-id [license]
  (or (member-license/plan-id license)
      (try
        (:id (<!!? (rp/fetch (config/stripe-private-key config) (plan-id license)
                             :managed-account (member-license/managed-account-id license))))
        (catch Throwable t
          nil))))


(defn- create-plan! [account license]
  (if-let [plan-id (maybe-existing-plan-id license)]
    {:id (str plan-id)}
    (<!!? (rp/create! (config/stripe-private-key config)
                      (plan-id license)
                      (plan-name account (member-license/property license))
                      (plan-amount license)
                      :month
                      :descriptor "STARCITY RENT"
                      :managed-account (member-license/managed-account-id license)))))


(defn- create-subscription! [customer plan-id license]
  (if-let [subs-id (member-license/subscription-id license)]
    {:id subs-id}
    (<!!? (rs/create! (config/stripe-private-key config)
                      (customer/id customer)
                      plan-id
                      :source (customer/bank-token customer)
                      :managed-account (member-license/managed-account-id license)
                      :trial-end (c/to-epoch (plan-start-date* license))
                      :fee-percent (-> license member-license/property property/ops-fee)))))


(defn subscribe!
  "Subscribe `account` to a rent plan. Assumes a `member-license` is already
  configured."
  [conn account]
  (let [license       (member-license/active (d/db conn) account)
        customer      (create-customer! (d/db conn) account license)
        {plan-id :id} (create-plan! account license)
        {sub-id :id}  (create-subscription! customer plan-id license)]
    (:db-after
     @(d/transact conn (tb/conj-when
                        [{:db/id                          (td/id license)
                          :member-license/customer        (td/id customer)
                          :member-license/subscription-id sub-id
                          :member-license/plan-id         plan-id}]
                        (when-not (p/entityd? customer) customer))))))

(s/fdef subscribe!
        :args (s/cat :conn p/conn? :account p/entity?)
        :ret p/db?)

(comment

  (let [conn    starcity.datomic/conn
        account (d/entity (d/db conn) [:account/email "member@test.com"])
        license (member-license/active conn account)
        subs-id (member-license/subscription-id (member-license/active starcity.datomic/conn account))]
    (clojure.core.async/<!! (rs/fetch (config/stripe-private-key config) subs-id
                                      :managed-account (member-license/managed-account-id license))))

  )
