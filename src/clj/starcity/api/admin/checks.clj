(ns starcity.api.admin.checks
  (:require [blueprints.models.check :as check]
            [blueprints.models.payment :as payment]
            [blueprints.models.security-deposit :as deposit]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [clojure.spec :as s]
            [compojure.core :refer [defroutes POST]]
            [datomic.api :as d]
            [plumbing.core :as plumbing]
            [starcity.datomic :refer [conn]]
            [starcity.util.response :as response]
            [starcity.util.validation :as uv]
            [taoensso.timbre :as timbre]
            [toolbelt.core :as tb]
            [toolbelt.predicates :as p]))

;; =============================================================================
;; Handlers
;; =============================================================================

(def check-validators
  {:number      [v/number]
   :name        [v/string]
   :status      [[v/member check/statuses]]
   :amount      [v/number]
   :date        [uv/inst]
   :received-on [uv/inst]
   :bank        [v/string]})


(defn- check-status->payment-status [status]
  (case status
    :check.status/received  :payment.status/pending
    :check.status/cleared   :payment.status/paid
    :check.status/cancelled :payment.status/failed
    :check.status/canceled  :payment.status/failed
    :check.status/bounced   :payment.status/failed
    :check.status/deposited :payment.status/pending))


;; =============================================================================
;; Update


(defn- update-check-tx [check params]
  (let [updated-check (check/update check params)]
    [updated-check]
    #_(if-let [py (check/rent-payment check)]
      (conj (rent-payment/update-check py check updated-check) updated-check)
      (let [payment (check/payment check)
            deposit (deposit/by-payment payment)]
        [updated-check
         {:db/id          (:db/id payment)
          :payment/status (check-status->payment-status (check/status updated-check))}]))))


(defn update-check!
  [conn check-id params]
  (let [check (d/entity (d/db conn) check-id)]
    @(d/transact conn (update-check-tx check params))
    {:result "ok"}))

;; =============================================================================
;; Create

(def create-validators
  "Validators for a new check. Differs from `check-validators` in that many/all
  attrs are required during creation."
  (reduce
   (fn [m [k v]]
     (assoc m k (conj v v/required)))
   {}
   check-validators))

(defn- check-tx
  "The transaction data to create a new check."
  [{:keys [number name status amount date received-on bank] :as params}]
  (check/create name amount date number
                :status status
                :received-on received-on
                :bank bank))


(defn- deposit-payment [deposit params]
  (let [check   (check-tx params)
        payment (payment/create (:amount params) (deposit/account deposit)
                                :status (check-status->payment-status (:status params))
                                :for :payment.for/deposit
                                :method :payment.method/check)]
    [check
     payment
     (payment/add-check payment check)
     (deposit/add-payment deposit payment)]))


(defn create-check!
  "Create a check on the security deposit idenfified by `deposit-id`."
  [conn {:keys [deposit-id payment-id] :as params}]
  (let [deposit (when deposit-id (d/entity (d/db conn) deposit-id))
        payment (when payment-id (d/entity (d/db conn) payment-id))
        check   (check-tx params)]
    @(d/transact conn (if deposit
                        (deposit-payment deposit params)
                        [(payment/add-check payment check) check]))
    {:result "ok"}))


(s/def ::deposit-id integer?)
(s/def ::payment-id integer?)
(s/fdef create-check
        :args (s/cat :conn p/conn?
                     :deposit-id integer?
                     :data (s/keys :req-un [:check/number
                                            :check/name
                                            :check/status
                                            :check/amount
                                            :check/date
                                            :check/received-on
                                            :check/bank]
                                   :opt-un [::deposit-id ::payment-id]))
        :ret (s/keys :req-un [::result]))


;; =============================================================================
;; Routes
;; =============================================================================

(defroutes routes
  (POST "/" []
        (fn [{params :params}]
          (let [vresult (b/validate params create-validators)
                params  (uv/valid? vresult #(update % :amount float))]
            (cond
              (and (nil? (:deposit-id params))
                   (nil? (:payment-id params)))
              (response/transit-malformed
               {:message "Either :deposit-id or :payment-id must be specified!"})

              (not params)
              (response/transit-malformed {:message (first (uv/errors vresult))})

              :otherwise (response/transit-ok (create-check! conn params)) ))))

  (POST "/:check-id" [check-id]
        (fn [{params :params}]
          (let [vresult (b/validate params check-validators)]
            (if-let [params (uv/valid? vresult #(plumbing/update-in-when % [:amount] float))]
              (response/transit-ok (update-check! conn (tb/str->int check-id) params))
              (response/transit-malformed {:message (first (uv/errors vresult))}))))))
