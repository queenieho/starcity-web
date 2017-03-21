(ns starcity.models.autopay
  (:require [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure.spec :as s]
            [datomic.api :as d]
            [starcity spec
             [datomic :refer [conn]]]
            [starcity.models
             [account :as account]
             [member-license :as member-license]
             [property :as property]
             [unit :as unit]]
            [starcity.models.stripe.customer :as customer]
            [starcity.services.stripe
             [plan :as plan]
             [subscription :as subscription]]
            starcity.spec.datomic
            [taoensso.timbre :as timbre]))

;;; Setup Status

;; TODO: Keywords when not using JSON
(s/def ::autopay-status #{"error" "none" "bank-needed" "unverified" "verified"})

(defn- fetch-setup-status
  [conn account]
  (try
    (let [c (customer/fetch (account/stripe-customer (d/db conn) account))]
      (cond
        (not (customer/has-bank-account? c))    "bank-needed"
        (customer/has-verified-bank-account? c) "verified"
        :otherwise                              "unverified"))
    (catch Exception e
      (timbre/error e ::fetch-customer {:account (account/email account)})
      "error")))

(defn setup-status
  "Produce the status of autopay setup for `account`."
  [conn account]
  (if-not (account/stripe-customer (d/db conn) account)
    "none"
    (fetch-setup-status conn account)))

(s/fdef setup-status
        :args (s/cat :conn :starcity.spec.datomic/connection
                     :account :starcity.spec.datomic/entity)
        :ret ::autopay-status)

(defn subscribed?
  "Is `account` subscribed to autopay?"
  [conn account]
  (let [ml (member-license/active conn account)]
    (member-license/autopay-on? ml)))

;;; Plan Data

(defn- is-first-day-of-month? [date-time]
  (= (t/day (c/to-date-time date-time)) 1))

(s/fdef is-first-day-of-month?
        :args (s/cat :date-time :starcity.spec/datetime)
        :ret boolean?)

(defn- is-past? [date-time]
  (t/before? date-time (t/now)))

(defn- first-day-next-month [date-time]
  (t/plus (t/first-day-of-the-month date-time)
          (t/months 1)))

(defn- plan-start-date*
  [member-license]
  (let [commencement (c/to-date-time (member-license/commencement member-license))]
    ;; If the commencement date is already the first day of the month, leave it
    ;; alone.
    (cond
      ;; The commencement date already passed, so the subscription needs to
      ;; start in the next calendar month. It's assumend that rent up until that
      ;; point is being collected with some other means.
      (is-past? commencement)               (first-day-next-month (t/now))
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
        :args (s/cat :member-license :starcity.spec.datomic/entity)
        :ret :starcity.spec/instant)

(defn- round
  "Round a double to the given precision (number of significant digits)"
  [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))

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
           (round 2)))))

(s/fdef prorated-amount
        :args (s/cat :member-license :starcity.spec.datomic/entity)
        :ret float?)

(s/def ::rent-amount float?)
(s/def ::commencement :starcity.spec/instant)
(s/def ::plan-start :starcity.spec/instant)
(s/def ::term integer?)
(s/def ::amount-due float?)
(s/def ::plan-details
  (s/keys :req-un [::rent-amount ::commencement ::term ::plan-start ::amount-due]))

(defn- plan-details
  "Retrieve the details of a member's license that are relevant to setting up
  autopay."
  [conn account]
  (let [active-license (member-license/active conn account)]
    {:rent-amount  (member-license/rate active-license)
     :commencement (member-license/commencement active-license)
     :end-date     (member-license/ends active-license)
     :term         (member-license/term active-license)
     :plan-start   (plan-start-date active-license)
     :amount-due   (prorated-amount active-license)}))

(s/fdef plan-details
        :args (s/cat :conn :starcity.spec.datomic/connection
                     :account :starcity.spec.datomic/entity)
        :ret ::plan-details)

;;; Setup

(s/def ::setup
  (s/keys :req-un [::status ::plan]))   ; TODO: (s/def ::plan ...)

(defn setup
  "Retrieve the setup data for `account` iff `account` should be 'setup'."
  [conn account]
  (when-not (subscribed? conn account)
    {:status (setup-status conn account)
     :plan   (plan-details conn account)}))

(s/fdef setup
        :args (s/cat :conn :starcity.spec.datomic/connection
                     :account :starcity.spec.datomic/entity)
        :ret (s/or :nothing nil?
                   :setup ::setup))

;;; Subscribe

(defn- plan-id [member-license]
  (str (:db/id member-license)))

(defn- plan-name [account property]
  (str (account/full-name account)
       "'s rent at "
       (property/name property)))

(defn- plan-amount [member-license]
  (int (* 100 (member-license/rate member-license))))

;; (defn- offset-days
;;   "Produce the number of days to delay charging rent by getting the difference
;;   between now and the plan's start date."
;;   [member-license]
;;   (t/in-days (t/interval (t/now) (plan-start-date* member-license))))

(def ^:private statement-descriptor
  "Starcity Rent")

(defn- create-plan! [member-license account property]
  (if-let [plan-id (member-license/plan-id member-license)]
    {:id (str plan-id)}
    (:body (plan/create! (plan-id member-license)
                         (plan-name account property)
                         (plan-amount member-license)
                         :month
                         :statement-descriptor statement-descriptor
                         :managed (property/managed-account-id property)))))

(defn- create-subscription! [customer plan-id member-license]
  (let [property (-> member-license member-license/unit unit/property)]
    (if-let [subscription-id (member-license/subscription-id member-license)]
      {:id subscription-id}
      (:body (subscription/create! (customer/id customer) plan-id
                                   :source (customer/bank-account-token customer)
                                   :managed (property/managed-account-id property)
                                   :trial-end (c/to-epoch (plan-start-date* member-license))
                                   :fee-percent (property/ops-fee property))))))

(defn subscribe!
  "Subscribe `account` to a rent plan. Assumes a `member-license` is already
  configured."
  [conn account]
  (let [active-license (member-license/active conn account)
        property       (-> active-license member-license/unit unit/property)
        customer       (customer/create-direct! account property)
        {plan-id :id}  (create-plan! active-license account property)
        {sub-id :id}   (create-subscription! customer plan-id active-license)]
    (:db-after
     @(d/transact conn [{:db/id                          (:db/id active-license)
                         :member-license/customer        (:db/id customer)
                         :member-license/subscription-id sub-id
                         :member-license/plan-id         plan-id}]))))

(s/fdef subscribe!
        :args (s/cat :conn :starcity.spec.datomic/connection
                     :account :starcity.spec.datomic/entity)
        :ret :starcity.spec.datomic/db)

(comment
  (let [account (d/entity (d/db starcity.datomic/conn) [:account/email "member@test.com"])
        ;; license  (member-license/active starcity.datomic/conn account)
        ;; property (d/entity (d/db starcity.datomic/conn) [:property/internal-name "52gilbert"])
        ]
    (subscribe! starcity.datomic/conn account)
    )

  )
