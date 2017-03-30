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
            [plumbing.core :as plumbing]))

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
   {:name           [v/required :message "Please provide the account holder's name."]
    :routing-number [v/required :message "Please provide your account's routing number."]
    :account-number [v/required :message "Please provide your account number."]}))

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
        :ret boolean?)

(defmethod complete? :default [_ _ _] false)

(defmethod complete? :deposit/method
  [_ account _]
  (let [deposit (deposit/by-account account)]
    (boolean (deposit/method deposit))))

(defmethod complete? :deposit.method/bank
  [conn account _]
  )

;; =====================================
;; Fetch

;; NOTE: the reason we fetch the data is for editing--what should we do in cases
;; where the data is not editable when complete (e.g. bank information while
;; awaiting security deposit verification)?

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
  [conn account step {method :method}]
  (let [method  (keyword "security-deposit.payment-method" method)
        deposit (deposit/by-account account)]
    @(d/transact conn [{:db/id                   (:db/id deposit)
                        :security-deposit/payment-method method}])))

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
            (if-let [errors (validate conn account step data)]
              (transit-malformed {:errors errors})
              (do
                (save! conn account step data)
                (transit-ok {:result (fetch conn account step)})))))))

(comment
  (fetch-all conn (account/by-email "onboarding@test.com"))

  )
