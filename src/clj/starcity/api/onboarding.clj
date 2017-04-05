(ns starcity.api.onboarding
  (:require [clojure.spec :as s]
            [compojure.core :refer [defroutes GET POST]]
            [starcity
             [auth :as auth]
             [datomic :refer [conn]]]
            [starcity.util.response :refer [transit-ok
                                            transit-malformed]]
            [starcity.util.validation :as validation]
            [toolbelt.predicates :as p]
            [starcity.models.security-deposit :as deposit]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [datomic.api :as d]
            [starcity.models.account :as account]
            [taoensso.timbre :as timbre]
            [plumbing.core :as plumbing]
            [starcity.models.stripe.customer :as customer]
            [starcity.models.charge :as charge]
            [starcity.models.msg :as msg]
            [starcity.models.stripe :as stripe]
            [starcity.models.approval :as approval]
            [starcity.models.unit :as unit]
            [starcity.models.property :as property]))

;; NOTE: Do we want to deal with dependencies like we do on the client? This may
;; make sense, since certain steps are moot in the absense of satisfied
;; prerequisites.
(def steps
  [:deposit/method
   :deposit.method/bank
   :deposit.method/verify
   :deposit/pay
   :services/moving
   :services/storage
   :services/customization
   :services/cleaning])

(s/def ::step (set steps))

;; =============================================================================
;; validate
;; =============================================================================

(defmulti validate*
  "Perform validation on `data` for `step`."
  (fn [conn account step data] step))

(defmethod validate* :default [_ _ _ _] nil)

(defmethod validate* :deposit/method
  [_ _ _ data]
  (b/validate
   data
   {:method [[v/required :message "Please choose a payment method."]
             [v/member #{"ach" "check"} :message "Please choose a valid payment method."]]}))

(defmethod validate* :deposit.method/bank
  [_ _ _ data]
  (b/validate
   data
   {:stripe-token [[v/required :message "Something went wrong; please try again or contact support."]]}))

(defmethod validate* :deposit.method/verify
  [_ _ _ data]
  (let [integer  [v/integer :message "Please enter your deposits in cents (whole numbers only)."]
        in-range [v/in-range [1 100] :message "Please enter a number between 1 and 100."]]
    (b/validate
     data
     {:amount-1 [[v/required :message "Please provide the first deposit amount."]
                 integer in-range]
      :amount-2 [[v/required :message "Please provide the second deposit amount."]
                 integer in-range]})))

(defmethod validate* :deposit/pay
  [_ _ _ data]
  (b/validate
   data
   {:method [[v/required :message "Please choose a payment option."]
             [v/member #{"partial" "full"} :message "Please choose a valid payment option."]]}))

(defn validate
  "Produces `nil` when `data` is valid for `step`, and a vector of error
  messages otherwise."
  [conn account step data]
  (when-let [vresult (validate* conn account step data)]
    (when-not (validation/valid? vresult)
      (validation/errors vresult))))

;; =============================================================================
;; fetch
;; =============================================================================

(s/def ::complete boolean?)
(s/def ::data map?)

;; =====================================
;; Complete

(defmulti complete?
  "Has `account` completed `step`?"
  (fn [conn account step] step))

(s/fdef complete?
        :args (s/cat :conn p/conn?
                     :account p/entity?
                     :step ::step)
        :ret (s/or :bool boolean? :nothing nil?))

(defmethod complete? :default [_ _ _] false)

(defmethod complete? :deposit/method
  [_ account _]
  (let [deposit (deposit/by-account account)]
    (boolean (deposit/method deposit))))

;; This step is complete when:
;; 1. A Stripe customer exists for this account (platform)
;; 2. Bank Verification has not failed for this customer
(defmethod complete? :deposit.method/bank
  [conn account _]
  (when-let [customer (account/stripe-customer (d/db conn) account)]
    (not (customer/verification-failed? (customer/fetch customer)))))

(defmethod complete? :deposit.method/verify
  [conn account _]
  (let [customer (account/stripe-customer (d/db conn) account)]
    (and (:stripe-customer/bank-account-token customer)
         (customer/has-verified-bank-account?
          (customer/fetch customer)))))

(defmethod complete? :deposit/pay
  [conn account _]
  (let [deposit (deposit/by-account account)]
    (and (:security-deposit/payment-type deposit)
         (or (some charge/is-pending? (deposit/charges deposit))
             (> (deposit/received deposit) 0)))))

(comment
  (complete? conn (account/by-email "onboarding@test.com") :deposit/pay)

  )

;; =====================================
;; Fetch

(defn- fetch-result
  "Use `validate` to determine whether or not `step` is complete. If it is,
  `validate` will produce `nil`."
  [conn account step data]
  {:complete (complete? conn account step)
   :data     data})

(defmulti fetch
  "Given a `step`, produce a map containing keys `complete` and `data`, where
  `complete` tells us whether or not this step has been completed, and `data` is
  the information entered by `account` in `step`."
  (fn [conn account step] step))

(s/fdef fetch
        :args (s/cat :conn p/conn?
                     :account p/entity?
                     :step ::step)
        :ret (s/or :response (s/keys :req-un [::complete ::data])
                   :nothing nil?))

(defmethod fetch :default [_ _ _] nil)

(defmethod fetch :deposit/method
  [conn account step]
  (let [deposit (deposit/by-account account)
        method  (deposit/method deposit)]
    (fetch-result conn account step (if method {:method (name method)} {}))))

(defmethod fetch :deposit.method/bank
  [conn account step]
  ;; No data required, just completion
  (fetch-result conn account step {}))

(defmethod fetch :deposit.method/verify
  [conn account step]
  ;; No data required, just completion
  (fetch-result conn account step {}))

(defmethod fetch :deposit/pay
  [conn account step]
  ;; No data required, just completion
  (fetch-result conn account step {}))

;; =============================================================================
;; fetch-all
;; =============================================================================

(defn fetch-all
  "This is just `fetch`, but performed on all steps: i.e. a reduction."
  [conn account]
  (reduce
   (fn [acc step]
     (plumbing/assoc-when acc step (fetch conn account step)))
   {}
   steps))

(s/fdef fetch-all
        :args (s/cat :conn p/conn? :account p/entity?)
        :ret map?)

;; =============================================================================
;; save
;; =============================================================================

(defmulti save!
  "Accepts a `step` and `data`. Persist the data and perform any necessary
  side-effects."
  (fn [conn account step data] step))

(s/fdef save!
        :args (s/cat :conn p/conn?
                     :account p/entity?
                     :step ::step
                     :data ::data))

(defmethod save! :default [conn account step _] (fetch conn account step))

(defmethod save! :deposit/method
  [conn account _ {method :method}]
  (let [method  (keyword "security-deposit.payment-method" method)
        deposit (deposit/by-account account)]
    @(d/transact conn [{:db/id                   (:db/id deposit)
                        :security-deposit/payment-method method}])))

(defmethod save! :deposit.method/bank
  [conn account _ {token :stripe-token}]
  (customer/create-platform! account token))

(defmethod save! :deposit.method/verify
  [conn account _ {:keys [amount-1 amount-2]}]
  (let [customer (account/stripe-customer (d/db conn) account)]
    (customer/verify-microdeposits customer amount-1 amount-2)))

(defn- charge-amount
  "Determine the correct amount to charge in cents given "
  [method deposit]
  (if (= "full" method)
    (* (deposit/required deposit) 100)
    50000))

(defn- create-charge
  [account deposit method]
  (let [customer (account/stripe-customer (d/db conn) account)]
    (stripe/create-charge! (:db/id account)
                           (charge-amount method deposit)
                           (:stripe-customer/bank-account-token customer)
                           :description (format "'%s' security deposit payment" method)
                           :customer-id (:stripe-customer/customer-id customer)
                           :managed-account (-> account
                                                account/approval
                                                approval/unit
                                                unit/property
                                                property/managed-account-id))))

(defn- charge-tx
  [deposit method charge]
  {:db/id                         (:db/id deposit)
   :security-deposit/payment-type (keyword "security-deposit.payment-type" method)
   :security-deposit/charges      (:db/id charge)})

(defmethod save! :deposit/pay
  [conn account step {method :method}]
  (let [deposit (deposit/by-account account)
        charge  (d/entity (d/db conn) (create-charge account deposit method))]
    (if (complete? conn account step)
      (throw (ex-info "Cannot charge customer for security deposit twice!"
                      {:account (:db/id account)}))
      @(d/transact conn [(charge-tx deposit method charge)
                         (msg/deposit-payment-made account charge)]))))

;; =============================================================================
;; Error Responses
;; =============================================================================

(defmulti on-error
  "Issue a custom response for `step` when exception `ex` is encountered.
  Rethrows by default."
  (fn [conn account step ex] step))

(defmethod on-error :default [_ _ _ ex]
  (throw ex))

(defmethod on-error :deposit.method/verify [_ _ _ ex]
  (if-let [message (get (ex-data ex) :message)]
    (transit-malformed {:errors [message]})
    (throw ex)))

;; =============================================================================
;; Routes & Handlers
;; =============================================================================

(defroutes routes
  (GET "/" []
       (fn [req]
         (transit-ok {:result (fetch-all conn (auth/requester req))})))

  (POST "/" []
        (fn [{:keys [params] :as req}]
          (let [{:keys [step data]} params
                account             (auth/requester req)]
            (timbre/debug "PARAMS:" params)
            (if-let [errors (validate conn account step data)]
              (transit-malformed {:errors errors})
              (try
                (save! conn account step data)
                (transit-ok {:result (fetch conn account step)})
                (catch Exception e
                  (on-error conn account step e))))))))

(comment
  (fetch-all conn (account/by-email "onboarding@test.com"))

  )
