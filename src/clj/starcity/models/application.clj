(ns starcity.models.application
  (:require [starcity.datomic.util :refer :all]
            [starcity.datomic.transaction :refer [replace-unique map-form->list-form]]
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

(defn- update-desired-availability-tx
  [application-id {availability :desired-availability}]
  (replace-unique application-id :rental-application/desired-availability availability))

(defn- update-desired-lease-tx
  [application-id {lease :desired-lease}]
  (when lease
    [[:db/add application-id :rental-application/desired-lease lease]]))

(defn- update-pet-tx
  [application-id {pet :pet}]
  (let [curr-pet (:rental-application/pet (one (d/db conn) application-id))]
    (cond
      ;; No more pet!
      (and (nil? pet) curr-pet) [[:db.fn/retractEntity (:db/id curr-pet)]]
      ;; If the pet has an id, it already exists
      (:id pet)                 [(merge {:db/id (:id pet)}
                                        (->> (dissoc pet :id)
                                             (ks->nsks :pet)))]
      ;; if there's no id for the pet, but there is a pet, it's a new one
      pet                       [{:db/id                  application-id
                                  :rental-application/pet (ks->nsks :pet pet)}])))

;; =============================================================================
;; API
;; =============================================================================

;; =============================================================================
;; Queries

(defn by-account
  "Retrieve an application by account id."
  [account-id]
  (qe1
   '[:find ?e
     :in $ ?acct
     :where
     [?acct :account/application ?e]]
   (d/db conn) account-id))

;; =============================================================================
;; Transactions

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

(s/def ::new-application
  (s/cat :account-id int?
         :desired-lease ::desired-lease
         :desired-availability (s/spec ::desired-availability)
         :opts (s/keys* :opt-un [::pet])))

(s/fdef create!
        :args ::new-application
        :ret  int?)

;; =====================================
;; update!

(defn update!
  [application-id params]
  (let [gen-tx (juxt update-desired-lease-tx
                     update-desired-availability-tx
                     update-pet-tx)
        tx     (->> (gen-tx application-id params)
                    (apply concat))]
    @(d/transact conn (vec tx))
    application-id))

(s/def ::update-application
  (s/cat :application-id int?
         :attributes (s/keys :opt-un [::desired-lease
                                      ::desired-availability
                                      ::pet])))

(s/fdef update!
        :args ::update-application
        :ret  int?)
