(ns starcity.models.application
  (:require [starcity.datomic.util :refer :all]
            [starcity.datomic.transaction :refer [replace-unique]]
            [starcity.models.util :refer :all]
            [starcity.datomic :refer [conn]]
            [starcity.config :refer [datomic-partition]]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when defnk]]
            [clojure.spec :as s]
            [clojure.string :refer [trim capitalize]]))

;; =============================================================================
;; Helper Specs
;; =============================================================================

;; =====================================
;; Pets

(s/def ::type #{:dog :cat})
(s/def ::breed string?)
(s/def ::weight pos-int?)
(s/def ::id pos-int?)

(defmulti pet-type :type)
(defmethod pet-type :dog [_] (s/keys :req-un [::type ::breed ::weight] :opt-un [::id]))
(defmethod pet-type :cat [_] (s/keys :req-un [::type] :opt-un [::id]))

(s/def ::pet (s/multi-spec pet-type :type))

;; =====================================
;; Application

(s/def ::desired-lease pos-int?)
(s/def ::desired-availability (s/+ :starcity.spec/date))

;; =============================================================================
;; Helpers
;; =============================================================================

;; =====================================
;; update!

(defn- desired-availability-update-tx
  [application {availability :desired-availability}]
  (replace-unique (:db/id application) :rental-application/desired-availability availability))

(defn- desired-lease-update-tx
  [application {lease :desired-lease}]
  (when lease
    [[:db/add (:db/id application) :rental-application/desired-lease lease]]))

(defn- pet-update-tx
  [application {pet :pet}]
  (let [curr (:rental-application/pet application)]
    (cond
      ;; No more pet!
      (and (nil? pet) curr) [[:db.fn/retractEntity (:db/id curr)]]
      ;; If the pet has an id, it already exists
      (:id pet)             [(merge {:db/id (:id pet)}
                                    (->> (dissoc pet :id)
                                         (ks->nsks :pet)))]
      ;; if there's no id for the pet, but there is a pet, it's a new one
      pet                   [{:db/id                  (:db/id application)
                              :rental-application/pet (ks->nsks :pet pet)}])))

(defn- address-update-tx
  [application {address :address}]
  (let [curr         (:rental-application/current-address application)
        address-data (ks->nsks :address address)]
    (if (nil? curr)
      [{:db/id                              (:db/id application)
        :rental-application/current-address address-data}]
      [(merge {:db/id (:db/id curr)} address-data)])))

(defn- income-update-tx
  [application {income :income-level}]
  [[:db/add (:db/id application) :rental-application/income income]])

;; =============================================================================
;; API
;; =============================================================================

(def income-levels
  "Allowed income levels."
  ["< 60k"
   "60k-70k"
   "70k-80k"
   "80k-90k"
   "90k-100k"
   "100k-110k"
   "110k-120k"
   "> 120k"])

;; =============================================================================
;; Queries

(defn by-account-id
  "Retrieve an application by account id."
  [account-id]
  (qe1
   '[:find ?e
     :in $ ?acct
     :where
     [?acct :account/application ?e]]
   (d/db conn) account-id))

(s/fdef by-account-id
        :args (s/cat :account-id integer?))

;; =============================================================================
;; Transactions

;; =====================================
;; update!

;; (defn update!
;;   [application-id params]
;;   (let [tx (->> ((gen-update-tx params) (one (d/db conn) application-id) params)
;;                 (apply concat))]
;;     (clojure.pprint/pprint tx)
;;     @(d/transact conn (vec tx))
;;     application-id))

(def update!
  (make-update-fn conn {:desired-lease        desired-lease-update-tx
                        :desired-availability desired-availability-update-tx
                        :pet                  pet-update-tx
                        :address              address-update-tx
                        :income-level         income-update-tx}))

(s/fdef update!
        :args (s/cat :application-id int?
                     :attributes (s/keys :opt-un [::desired-lease
                                                  ::desired-availability
                                                  ::pet]))
        :ret  int?)

;; =====================================
;; create!

(defn create!
  "Create a new rental application for `account-id'."
  [account-id desired-lease desired-availability & {:keys [pet]}]
  (let [tid (d/tempid (datomic-partition))
        pet (when pet (ks->nsks :pet pet))
        ent (-> {:db/id                tid
                 :desired-lease        desired-lease
                 :desired-availability desired-availability}
                (assoc-when :pet pet)
                (assoc :account/_application account-id))
        tx  @(d/transact conn [(ks->nsks :rental-application ent)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))

(s/fdef create!
        :args (s/cat :account-id int?
                     :desired-lease ::desired-lease
                     :desired-availability (s/spec ::desired-availability)
                     :opts (s/keys* :opt-un [::pet]))
        :ret  int?)
