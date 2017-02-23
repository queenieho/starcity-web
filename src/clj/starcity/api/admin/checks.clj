(ns starcity.api.admin.checks
  (:require [bouncer
             [core :as b]
             [validators :as v]]
            [clojure.spec :as s]
            [compojure.core :refer [defroutes POST]]
            [datomic.api :as d]
            [plumbing.core :refer [update-in-when]]
            [starcity.datomic :refer [conn]]
            [starcity.models
             [check :as check]
             [rent-payment :as rent-payment]
             [security-deposit :as deposit]]
            [starcity.util
             [response :as response]
             [validation :as uv]]
            [toolbelt
             [core :as tb :refer [str->int]]
             [predicates :as p]]))

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

;; =============================================================================
;; Update

(defn- update-check-tx [check params]
  (let [deposit       (check/security-deposit check)
        payment       (check/rent-payment check)
        updated-check (check/update check params)]
    (if deposit
      [updated-check (deposit/update-check deposit check updated-check)]
      (conj (rent-payment/update-check payment check updated-check) updated-check))))

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

(defn create-check!
  "Create a check on the security deposit idenfified by `deposit-id`."
  [conn {:keys [deposit-id payment-id] :as params}]
  (let [deposit (when deposit-id (d/entity (d/db conn) deposit-id))
        payment (when payment-id (d/entity (d/db conn) payment-id))]
    @(d/transact conn [(if deposit
                         (deposit/add-check deposit (check-tx params))
                         (rent-payment/add-check payment (check-tx params)))])
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
            (if-let [params (uv/valid? vresult #(update-in-when % [:amount] float))]
              (response/transit-ok (update-check! conn (str->int check-id) params))
              (response/transit-malformed {:message (first (uv/errors vresult))}))))))
