(ns starcity.models.application
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when]]
            [starcity
             [config :refer [datomic-partition]]
             [datomic :refer [conn]]]
            [starcity.models.util :refer :all]
            [starcity.spec]))

;; =============================================================================
;; Helper Specs
;; =============================================================================

;; =====================================
;; Pets

(s/def ::type #{:dog :cat})
(s/def ::breed string?)
(s/def ::weight pos-int?)
(s/def ::id int?)

(defmulti pet-type :type)
(defmethod pet-type :dog [_] (s/keys :req-un [::type ::breed ::weight] :opt-un [::id]))
(defmethod pet-type :cat [_] (s/keys :req-un [::type] :opt-un [::id]))
(defmethod pet-type nil [_] nil?)

(s/def ::pet (s/multi-spec pet-type :type))

;; =====================================
;; Application

(s/def ::desired-license int?)
(s/def ::desired-availability :starcity.spec/date)
(s/def ::desired-properties (s/spec (s/+ int?)))

;; =============================================================================
;; Helpers
;; =============================================================================

;; =====================================
;; update!

(defn- desired-availability-update-tx
  [application {availability :desired-availability}]
  [[:db/add (:db/id application) :member-application/desired-availability availability]])

(defn- desired-properties-update-tx
  [application {properties :desired-properties}]
  (replace-unique conn (:db/id application) :member-application/desired-properties properties))

(defn- desired-license-update-tx
  [application {license :desired-license}]
  (when license
    [[:db/add (:db/id application) :member-application/desired-license license]]))

(defn- pet-update-tx
  [application {pet :pet}]
  (let [curr (:member-application/pet application)]
    (cond
      ;; No more pet!
      (and (nil? pet) curr) [[:db.fn/retractEntity (:db/id curr)]]
      ;; If the pet has an id, it already exists
      (:id pet)             [(merge {:db/id (:id pet)}
                                    (->> (dissoc pet :id)
                                         (ks->nsks :pet)))]
      ;; if there's no id for the pet, but there is a pet, it's a new one
      pet                   [{:db/id                  (:db/id application)
                              :member-application/pet (ks->nsks :pet pet)}])))

(defn- address-update-tx
  [application {address :address}]
  (let [curr         (:member-application/current-address application)
        address-data (ks->nsks :address address)]
    (if (nil? curr)
      [{:db/id                              (:db/id application)
        :member-application/current-address address-data}]
      [(merge {:db/id (:db/id curr)} address-data)])))

;; =============================================================================
;; API
;; =============================================================================

(def sections
  "Sections of the application process"
  #{:logistics :personal :community})

;; =============================================================================
;; Queries

(defn by-account-id
  "Retrieve an application by account id."
  [account-id]
  (qe1
   '[:find ?e
     :in $ ?acct
     :where
     [?acct :account/member-application ?e]]
   (d/db conn) account-id))

(s/fdef by-account-id
        :args (s/cat :account-id integer?))

(defn logistics-complete?
  "Returns true if the logistics section of the application can be considered
  complete."
  [application-id]
  (let [ks   [:member-application/desired-license
              :member-application/desired-availability]
        data (d/pull (d/db conn) ks application-id)]
    (every? (comp not nil?) ((apply juxt ks) data))))

(s/fdef logistics-complete?
        :args (s/cat :application-id int?)
        :ret  boolean?)

(defn personal-information-complete?
  "Returns true if the personal information section of the application can be
  considered complete."
  [application-id]
  (let [pattern [:member-application/current-address
                 {:account/_member-application [:account/dob :plaid/_account]}]
        data    (d/pull (d/db conn) pattern application-id)
        acct    (get-in data [:account/_member-application 0])
        plaid   (get-in acct [:plaid/_account 0])]
    (not (or (nil? (:member-application/current-address data))
             (nil? (:account/dob acct))
             (nil? plaid)))))

(s/fdef personal-information-complete?
        :args (s/cat :application-id int?)
        :ret  boolean?)

(defn community-fitness-complete?
  "Returns true if the community fitness section of the application can be
  considered complete."
  [application-id]
  (let [pattern [{:member-application/community-fitness
                  [:community-fitness/prior-community-housing
                   :community-fitness/why-interested
                   :community-fitness/skills
                   :community-fitness/free-time]}]
        data    (:member-application/community-fitness
                 (d/pull (d/db conn) pattern application-id))]
    (boolean
     (and (:community-fitness/prior-community-housing data)
          (:community-fitness/why-interested data)
          (:community-fitness/skills data)
          (:community-fitness/free-time data)))))


(s/def ::step #{:logistics :personal :community :submit})
(s/def ::steps (s/and set? (s/* ::step)))

(defn current-steps
  "Given an account id, return a set of allowed steps in the application process."
  [account-id]
  (let [current #{:logistics}]
    (if-let [application-id (:db/id (by-account-id account-id))]
      (cond-> current
        (logistics-complete? application-id) (conj :personal)
        (personal-information-complete? application-id) (conj :community)
        (community-fitness-complete? application-id) (conj :submit))
      current)))

(s/fdef current-steps
        :args (s/cat :account-id int?)
        :ret  ::steps)

(defn locked?
  "Is the application for this user locked?"
  [account-id]
  (let [ent (by-account-id account-id)]
    (boolean (:member-application/locked ent))))

;; =============================================================================
;; Transactions

;; =====================================
;; update!

(def update!
  (make-update-fn
   conn
   {:desired-license      desired-license-update-tx
    :desired-availability desired-availability-update-tx
    :desired-properties   desired-properties-update-tx
    :pet                  pet-update-tx
    :address              address-update-tx}))

(s/fdef update!
        :args (s/cat :application-id int?
                     :attributes (s/keys :opt-un [::desired-license
                                                  ::desired-availability
                                                  ::desired-properties
                                                  ::pet]))
        :ret  int?)

;; =====================================
;; complete!

(defn complete!
  [account-id stripe-id]
  (let [application-id (:db/id (by-account-id account-id))
        tid            (d/tempid (datomic-partition))]
    @(d/transact conn [{:db/id                           application-id
                        :member-application/locked       true
                        :member-application/submitted-at (java.util.Date.)}
                       {:db/id            tid
                        :charge/stripe-id stripe-id
                        :charge/account   account-id
                        :charge/purpose   "application fee"}])))

;; =====================================
;; update-community-fitness!

(s/def ::prior-community-housing string?)
(s/def ::skills string?)
(s/def ::why-interested string?)

(defn update-community-fitness!
  [application-id params]
  (let [application (d/entity (d/db conn) application-id)
        ent         (ks->nsks :community-fitness params)]
    (if-let [cf-id (-> application :member-application/community-fitness :db/id)]
      @(d/transact conn [(assoc ent :db/id cf-id)])
      @(d/transact conn [{:db/id application-id
                          :member-application/community-fitness ent}]))))

(s/fdef update-community-fitness!
        :args (s/cat :application-id int?
                     :attributes (s/keys :opt-un [::prior-community-housing
                                                  ::skills
                                                  ::why-interested
                                                  ::dealbreakers
                                                  ::free-time])))

;; =====================================
;; create!

(defn create!
  "Create a new rental application for `account-id'."
  [account-id desired-properties desired-license desired-availability & {:keys [pet]}]
  (let [tid (d/tempid (datomic-partition))
        pet (when pet (ks->nsks :pet pet))
        ent (-> {:db/id                tid
                 :desired-license      desired-license
                 :desired-availability desired-availability
                 :desired-properties   desired-properties}
                (assoc-when :pet pet)
                (assoc :account/_member-application account-id))
        tx  @(d/transact conn [(ks->nsks :member-application ent)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))

(s/fdef create!
        :args (s/cat :account-id int?
                     :desired-properties ::desired-properties
                     :desired-license ::desired-license
                     :desired-availability ::desired-availability
                     :opts (s/keys* :opt-un [::pet]))
        :ret  int?)
