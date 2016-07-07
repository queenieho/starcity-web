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
(defmethod pet-type nil [_] nil?)

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

;; =============================================================================
;; API
;; =============================================================================

(def sections
  "Sections of the application process"
  #{:logistics :checks :community})

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

(defn logistics-complete?
  "Returns true if the logistics section of the application can be considered
  complete."
  [application-id]
  (let [ks   [:rental-application/desired-lease
              :rental-application/desired-availability]
        data (d/pull (d/db conn) ks application-id)]
    (every? (comp not nil?) ((apply juxt ks) data))))

(s/fdef logistics-complete?
        :args (s/cat :application-id int?)
        :ret  boolean?)

(defn personal-information-complete?
  "Returns true if the logistics section of the application can be considered
  complete."
  [application-id]
  (let [pattern [:rental-application/current-address
                 {:account/_application [:account/dob :plaid/_account]}]
        data    (d/pull (d/db conn) pattern application-id)
        acct    (get-in data [:account/_application 0])
        plaid   (get-in acct [:plaid/_account 0])]
    (not (or (nil? (:rental-application/current-address data))
             (nil? (:account/dob acct))
             (nil? plaid)))))

(s/fdef personal-information-complete?
        :args (s/cat :application-id int?)
        :ret  boolean?)


;; TODO: Rename :checks
(s/def ::step #{:logistics :checks :community})
(s/def ::steps (s/and set? (s/* ::step)))

(defn current-steps
  "Given an account id, return a set of allowed steps in the application process."
  [account-id]
  (let [current #{:logistics}]
    (if-let [application-id (:db/id (by-account-id account-id))]
      (cond-> current
        (logistics-complete? application-id) (conj :checks)
        (personal-information-complete? application-id) (conj :community))
      current)))

(s/fdef current-steps
        :args (s/cat :account-id int?)
        :ret  ::steps)

;; =============================================================================
;; Transactions

;; =====================================
;; update!

(def update!
  (make-update-fn {:desired-lease        desired-lease-update-tx
                   :desired-availability desired-availability-update-tx
                   :pet                  pet-update-tx
                   :address              address-update-tx}))

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
