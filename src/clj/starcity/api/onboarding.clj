(ns starcity.api.onboarding
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [clj-time
             [coerce :as c]
             [core :as t]]
            [clojure
             [set :as set]
             [spec :as s]]
            [compojure.core :refer [defroutes GET POST]]
            [datomic.api :as d]
            [plumbing.core :as plumbing]
            [starcity
             [auth :as auth]
             [datomic :refer [conn]]
             [util :refer [beginning-of-day]]]
            [starcity.models
             [account :as account]
             [approval :as approval]
             [catalogue :as catalogue]
             [charge :as charge]
             [msg :as msg]
             [news :as news]
             [onboard :as onboard]
             [order :as order]
             [property :as property]
             [security-deposit :as deposit]
             [service :as service]
             [stripe :as stripe]
             [unit :as unit]]
            [starcity.models.stripe.customer :as customer]
            [starcity.services.stripe.sources :as sources]
            [starcity.util
             [request :as req]
             [response :as res :refer [transit-malformed transit-ok]]
             [validation :as validation]]
            [taoensso.timbre :as timbre]
            [toolbelt.predicates :as p]))

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
   :services/customize
   :services/cleaning
   :services/upgrades])

(s/def ::step (set steps))

;; =============================================================================
;; validate
;; =============================================================================

(defmulti validations
  "Produce the validations for `step`."
  (fn [conn account step] step))

(defmethod validations :default [_ _ _] {})

(defmethod validations :deposit/method
  [_ _ _]
  {:method [[v/required :message "Please choose a payment method."]
            [v/member #{"ach" "check"} :message "Please choose a valid payment method."]]})

(defmethod validations :deposit.method/bank
  [_ _ _]
  {:stripe-token [[v/required :message "Something went wrong; please try again or contact support."]]})

(defmethod validations :deposit.method/verify
  [_ _ _]
  (let [integer  [v/integer :message "Please enter your deposits in cents (whole numbers only)."]
        in-range [v/in-range [1 100] :message "Please enter a number between one and 100."]]
    {:amount-1 [[v/required :message "Please provide the first deposit amount."]
                integer in-range]
     :amount-2 [[v/required :message "Please provide the second deposit amount."]
                integer in-range]}))

(defmethod validations :deposit/pay
  [_ _ _]
  {:method [[v/required :message "Please choose a payment option."]
            [v/member #{"partial" "full"} :message "Please choose a valid payment option."]]})

;; If `needed` is false, no other reqs
(defmethod validations :services/moving
  [_ account _]
  (letfn [(-commencement [account]
            (-> account approval/by-account approval/move-in beginning-of-day))
          (-after-commencement? [date]
            (or (= date (-commencement account))
                (t/after? (c/to-date-time date) (c/to-date-time (-commencement account)))))]
    {:needed [[v/required :message "Please indicate whether or not you need moving assistance."]]
     :date   [[v/required :message "Please provide a move-in date." :pre (comp true? :needed)]
              [-after-commencement? :message "Your move-in date cannot be before your license commences." :pre (comp true? :needed)]]
     :time   [[v/required :message "Please provide a move-in time." :pre (comp true? :needed)]]}))

;; NOTE: Skipping validations on catalogue services atm
;; TODO: Validations on catalogue services

(defn validate
  "Produces `nil` when `data` is valid for `step`, and a vector of error
  messages otherwise."
  [conn account step data]
  (when-let [vresult (b/validate data (validations conn account step))]
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
  (if (= (-> account deposit/by-account deposit/method)
         :security-deposit.payment-method/check)
    nil
    (if-let [customer (account/stripe-customer (d/db conn) account)]
      (not (customer/verification-failed? (customer/fetch customer)))
      false)))

(defmethod complete? :deposit.method/verify
  [conn account _]
  (let [customer (account/stripe-customer (d/db conn) account)]
    (and (:stripe-customer/bank-account-token customer)
         (customer/has-verified-bank-account?
          (customer/fetch customer)))))

(defmethod complete? :deposit/pay
  [conn account _]
  (let [deposit (deposit/by-account account)]
    (boolean (or (some charge/is-pending? (deposit/charges deposit))
                 (> (deposit/received deposit) 0)))))

(defmethod complete? :services/moving
  [conn account _]
  (let [onboard (onboard/by-account account)]
    (or (onboard/seen-moving? onboard)
        (and (inst? (onboard/move-in onboard))
             (let [s (service/moving-assistance (d/db conn))]
               (order/exists? (d/db conn) account s))))))

(defmethod complete? :services/storage
  [conn account _]
  (let [onboard (onboard/by-account account)]
    (onboard/seen-storage? onboard)))

(defmethod complete? :services/customize
  [conn account _]
  (let [onboard (onboard/by-account account)]
    (onboard/seen-customize? onboard)))

(defmethod complete? :services/cleaning
  [conn account _]
  (let [onboard (onboard/by-account account)]
    (onboard/seen-cleaning? onboard)))

(defmethod complete? :services/upgrades
  [conn account _]
  (let [onboard (onboard/by-account account)]
    (onboard/seen-upgrades? onboard)))

;; =====================================
;; Fetch

(defn- order-params
  "Produce the client-side parameters for services ordered by `account` from
  `catalogue`."
  [db account catalogue]
  (->> (service/ordered-from-catalogue db account catalogue)
       (d/q '[:find ?o ?s
              :in $ ?a [?s ...]
              :where
              [?o :order/account ?a]
              [?o :order/service ?s]]
            db (:db/id account))
       (reduce
        (fn [acc [order-id service-id]]
          (let [order (d/entity db order-id)]
            (-> {service-id
                 (plumbing/assoc-when
                  {}
                  :quantity (order/quantity order)
                  :desc (order/desc order)
                  :variant (:db/id (order/variant order)))}
                (merge acc))))
        {})))

(s/def ::quantity (s/and pos? number?))
(s/def ::desc string?)
(s/def ::variant integer?)
(s/def ::order-params
  (s/map-of integer? (s/keys :opt-un [::quantity ::desc ::variant])))

(s/fdef order-params
        :args (s/cat :db p/db?
                     :account p/entity?
                     :catalogue p/entity?)
        :ret ::order-params)


(defmulti fdata
  "Given a `step`, produce a map containing keys `complete` and `data`, where
  `complete` tells us whether or not this step has been completed, and `data` is
  the information entered by `account` in `step`."
  (fn [conn account step] step))

(defn fetch
  [conn account step]
  {:complete (complete? conn account step)
   :data     (fdata conn account step)})

(s/fdef fetch
        :args (s/cat :conn p/conn?
                     :account p/entity?
                     :step ::step)
        :ret (s/or :response (s/keys :req-un [::complete ::data])
                   :nothing nil?))

(defmethod fdata :default [_ _ _] nil)

(defmethod fdata :deposit/method
  [conn account step]
  (let [deposit (deposit/by-account account)
        method  (deposit/method deposit)]
    (if method {:method (name method)} {})))

(defmethod fdata :deposit.method/bank
  [conn account step]
  ;; No data required, just completion
  {})

(defmethod fdata :deposit.method/verify
  [conn account step]
  ;; No data required, just completion
  {})

(defmethod fdata :deposit/pay
  [conn account step]
  ;; No data required, just completion
  {})

(defmethod fdata :services/moving
  [conn account step]
  (let [onboard (onboard/by-account account)
        service (service/moving-assistance (d/db conn))
        order   (order/by-account (d/db conn) account service)]
    {:needed (when (onboard/seen-moving? onboard) (p/entity? order))
     :date   (onboard/move-in onboard)
     :time   (onboard/move-in onboard)}))

(defmethod fdata :services/storage
  [conn account step]
  (let [onboard   (onboard/by-account account)
        property  (-> account approval/by-account approval/property)
        catalogue (catalogue/storage (d/db conn) property)]
    {:seen      (onboard/seen-storage? onboard)
     :orders    (order-params (d/db conn) account catalogue)
     :catalogue (catalogue/clientize catalogue)}))

(defmethod fdata :services/customize
  [conn account step]
  (let [onboard   (onboard/by-account account)
        catalogue (catalogue/customization (d/db conn))]
    {:seen      (onboard/seen-customize? onboard)
     :orders    (order-params (d/db conn) account catalogue)
     :catalogue (catalogue/clientize catalogue)}))

(defmethod fdata :services/cleaning
  [_ account _]
  (let [onboard   (onboard/by-account account)
        catalogue (catalogue/cleaning+laundry (d/db conn))]
    {:seen      (onboard/seen-cleaning? onboard)
     :orders    (order-params (d/db conn) account catalogue)
     :catalogue (catalogue/clientize catalogue)}))

(defmethod fdata :services/upgrades
  [_ account _]
  (let [onboard   (onboard/by-account account)
        property  (-> account approval/by-account approval/property)
        catalogue (catalogue/upgrades (d/db conn) property)]
    {:seen      (onboard/seen-upgrades? onboard)
     :orders    (order-params (d/db conn) account catalogue)
     :catalogue (catalogue/clientize catalogue)}))

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

(defmethod save! :default [conn account step _]
  (timbre/debugf "no `save!` method implemented for %s" step))

;; =============================================================================
;; Security Deposit

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
  [conn account deposit method]
  (let [customer (account/stripe-customer (d/db conn) account)]
    (stripe/create-charge! conn
                           (:db/id account)
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
        charge  (d/entity (d/db conn) (create-charge conn account deposit method))]
    (if (complete? conn account step)
      (throw (ex-info "Cannot charge customer for security deposit twice!"
                      {:account (:db/id account)}))
      @(d/transact conn [(charge-tx deposit method charge)
                         (msg/deposit-payment-made account charge)]))))

;; =============================================================================
;; Services

(defn- update-orders
  "Update all orders for services in `params` that are also in `existing`."
  [db account params existing]
  (->> (reduce (fn [acc [k v]] (if (empty? v) acc (conj acc k))) #{} params) ; remove keys w/ null vals
       (set/intersection (set existing))
       ;; find tuples of [order service] given service-ids to update
       (d/q '[:find ?o ?s
              :in $ ?a [?s ...]
              :where
              [?o :order/account ?a]
              [?o :order/service ?s]]
            db (:db/id account))
       ;; gen txes
       (map
        (fn [[order-id service-id]]
          (let [{:keys [quantity desc variant] :as params} (get params service-id)
                order                                      (d/entity db order-id)]
            (timbre/debug "update params:" order-id params)
            (->> (plumbing/assoc-when
                  {}
                  :quantity (when-let [q quantity] (float q))
                  :desc     desc
                  :variant  variant)
                 (order/update order)))))))

(defn- remove-orders
  "Remoe all orders for services in `existing` but not in `params`."
  [db account params existing]
  (->> (set/difference (set existing) (set (keys params))) ; svc ids whose orders should be removed
       ;; find orders for services
       (d/q '[:find [?o ...]
              :in $ ?a [?s ...]
              :where
              [?o :order/account ?a]
              [?o :order/service ?s]]
            db (:db/id account))
       ;; gen txes
       (map (partial conj [:db.fn/retractEntity]))))

(defn- create-orders
  "Create new orders for all services in `params`."
  [db account params existing]
  (->> (set/difference (set (keys params)) (set existing))
       (select-keys params)
       ;; gen txes
       (map
        (fn [[service-id params]]
          (let [service (d/entity db service-id)]
            (timbre/debug "service:" (:service/code service) "params:" params)
            (order/create account service
                          (plumbing/assoc-when
                           {}
                           :quantity (when-let [q (:quantity params)] (float q))
                           :desc (:desc params)
                           :variant (:variant params))))))))

(defn orders-tx
  "Given server-side `params` and a `catalogue`, generate a transaction to
  create orders for newly requested services, remove orders that are no longer
  requested, and update any orders that may have changed."
  [db account catalogue params]
  (let [existing (service/ordered-from-catalogue db account catalogue)]
    (->> ((juxt create-orders update-orders remove-orders)
          db account params existing)
         (apply concat))))

(s/fdef orders-tx
        :args (s/cat :db p/db?
                     :account p/entity?
                     :catalogue p/entity?
                     :params ::order-params)
        :ret vector?)

;; =====================================
;; Moving Assistance

(defn- combine [date time]
  (let [date (c/to-date-time date)
        time (c/to-date-time time)]
    (-> (t/date-time (t/year date) (t/month date) (t/day date) (t/hour time) (t/minute time))
        (c/to-date))))

(defn- add-move-in-tx
  [db onboard move-in]
  (let [service (service/moving-assistance db)]
    (plumbing/conj-when
     [{:db/id           (:db/id onboard)
       :onboard/move-in move-in}]
     ;; When there's not an moving-assistance order, create one.
     (when-not (order/exists? db (onboard/account onboard) service)
       (order/create (onboard/account onboard) service)))))

(defn- remove-move-in-tx
  [db account onboard]
  (let [retract-move-in (when-let [v (onboard/move-in onboard)]
                          [:db/retract (:db/id onboard) :onboard/move-in v])
        retract-order   (order/remove-existing db account (service/moving-assistance db))]
    (plumbing/conj-when [] retract-move-in retract-order)))

(defmethod save! :services/moving
  [conn account step {:keys [needed date time]}]
  (let [onboard (onboard/by-account account)
        service (service/moving-assistance (d/db conn))
        order   (order/by-account (d/db conn) account service)]
    @(d/transact conn (-> (if needed
                            (add-move-in-tx (d/db conn) onboard (combine date time))
                            (remove-move-in-tx (d/db conn) account onboard))
                          (conj (onboard/add-seen onboard step))))))

;; =====================================

(defmethod save! :services/storage
  [conn account step {seen :seen :as params}]
  (let [onboard   (onboard/by-account account)
        property  (-> account approval/by-account approval/property)
        catalogue (catalogue/storage (d/db conn) property)]
    @(d/transact conn (conj
                       (orders-tx (d/db conn) account catalogue (:orders params))
                       (onboard/add-seen onboard step)))))

(defmethod save! :services/customize
  [conn account step {seen :seen :as params}]
  (let [onboard   (onboard/by-account account)
        catalogue (catalogue/customization (d/db conn))]
    @(d/transact conn (conj
                       (orders-tx (d/db conn) account catalogue (:orders params))
                       (onboard/add-seen onboard step)))))

(defmethod save! :services/cleaning
  [conn account step {seen :seen :as params}]
  (let [onboard   (onboard/by-account account)
        catalogue (catalogue/cleaning+laundry (d/db conn))]
    @(d/transact conn (conj
                       (orders-tx (d/db conn) account catalogue (:orders params))
                       (onboard/add-seen onboard step)))))

(defmethod save! :services/upgrades
  [conn account step {seen :seen :as params}]
  (let [onboard   (onboard/by-account account)
        property  (-> account approval/by-account approval/property)
        catalogue (catalogue/upgrades (d/db conn) property)]
    @(d/transact conn (conj
                       (orders-tx (d/db conn) account catalogue (:orders params))
                       (onboard/add-seen onboard step)))))

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

(defn- is-finished? [db account]
  (let [deposit (deposit/by-account account)
        onboard (onboard/by-account account)]
    (and (boolean (or (some charge/is-pending? (deposit/charges deposit))
                      (> (deposit/received deposit) 0)))
         (onboard/seen-cleaning? onboard)
         (onboard/seen-customize? onboard)
         (onboard/seen-moving? onboard)
         (onboard/seen-storage? onboard)
         (onboard/seen-upgrades? onboard))))

(defn- finish! [conn account {token :token}]
  (if-let [customer (account/stripe-customer (d/db conn) account)]
    (sources/create! (customer/id customer) token)
    (customer/create-platform! account token))
  @(d/transact conn (conj (account/promote account)
                          (news/welcome account)
                          (news/autopay account)
                          (msg/promoted account))))

(defn finish-handler
  [{:keys [params session] :as req}]
  (let [account  (req/requester (d/db conn) req)
        finished (is-finished? (d/db conn) account)
        orders   (order/orders (d/db conn) account)]
    (cond
      (not finished)
      (res/transit-unprocessable {:error "Cannot submit; onboarding is unfinished."})

      ;; If there are orders, ensure that a token has been passed along.
      (and (> (count orders) 0) (not (:token params)))
      (res/transit-malformed {:error "Your credit card details are required."})

      :otherwise (let [session (assoc-in session [:identity :account/role] account/member)]
                   (finish! conn account params)
                   (-> (res/transit-ok {:message "ok"})
                       (assoc :session session))))))

(defroutes routes
  (GET "/" []
       (fn [req]
         (transit-ok {:result (fetch-all conn (auth/requester req))})))

  (POST "/" []
        (fn [{:keys [params] :as req}]
          (let [{:keys [step data]} params
                account             (req/requester (d/db conn) req)]
            (timbre/debug "PARAMS:" params)
            (if-let [errors (validate conn account step data)]
              (transit-malformed {:errors errors})
              (try
                (save! conn account step data)
                (transit-ok {:result (fetch conn (d/entity (d/db conn) (:db/id account)) step)})
                (catch Exception e
                  (on-error conn account step e)))))))

  (POST "/finish" [] finish-handler))

(comment
  (fetch-all conn (account/by-email (d/db conn) "onboarding@test.com"))

  )
