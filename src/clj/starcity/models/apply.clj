(ns starcity.models.apply
  (:require [starcity.models
             [application :as application]
             [license :as license]]
            [starcity.models.util :refer :all]
            [starcity.models.util.update :refer [replace-unique2]]
            [starcity.datomic :refer [conn tempid]]
            [starcity.spec]
            [clojure.spec :as s]
            [datomic.api :as d]
            [taoensso.timbre :as timbre]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [plumbing.core :refer [assoc-when]])
  (:refer-clojure :exclude [update]))

(timbre/refer-timbre)

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- account-id [application]
  (:db/id (first (:account/_member-application application))))

(defn- community-safety [application]
  ;; TODO: This correct?
  (-> application :account/_member-application first :community-safety/_account first))

;; =============================================================================
;; Initialization
;; =============================================================================

(defn fetch-properties
  "Fetch all properties "
  []
  (letfn [(-count-available [units]
            (count (filter :unit/available-on units)))]
    (->> (d/q '[:find ?e :where [?e :property/name _]] (d/db conn))
         (map first)
         (d/pull-many (d/db conn) [:property/upcoming
                                   :property/name
                                   :property/internal-name
                                   :property/available-on
                                   {:property/licenses [:property-license/license
                                                        :property-license/base-price]}
                                   {:property/units [:unit/available-on]}])
         (map #(update-in % [:property/units] -count-available)))))

(defn initial-data
  "Required information to the application client."
  []
  {:properties (fetch-properties)
   :licenses   (license/licenses)})

;; =============================================================================
;; Progress
;; =============================================================================

;; =============================================================================
;; Internal

(defn- fetch
  [account-id]
  (d/pull (d/db conn)
          [:account/first-name
           :account/last-name
           :account/middle-name
           :account/phone-number
           :account/dob
           {:income-file/_account [:income-file/path]}
           {:community-safety/_account
            [:db/id :community-safety/consent-given?]}
           {:account/member-application
            [{:member-application/desired-properties ; properties
              [:property/internal-name]}
             {:member-application/desired-license [:db/id]} ; license
             :member-application/desired-availability       ; move-in
             {:member-application/pet
              [:pet/type :pet/breed :pet/weight]}
             {:member-application/current-address ; address
              [:address/state :address/city :address/postal-code]}
             {:member-application/community-fitness ; community fitness
              [:community-fitness/why-interested
               :community-fitness/free-time
               :community-fitness/dealbreakers
               :community-fitness/skills
               :community-fitness/prior-community-housing]}]}]
          account-id))

;; =====================================
;; Account

(defn- parse-account
  [{:keys [:account/first-name
           :account/last-name
           :account/middle-name
           :account/phone-number
           :account/dob]}]
  {:account
   (-> {:name {:first  first-name
               :middle middle-name
               :last   last-name}}
       (assoc-when :phone-number phone-number
                   :dob dob))})

;; =====================================
;; Communities

(defn- parse-communities [data]
  (let [communities (get-in data [:account/member-application
                                  :member-application/desired-properties])]
    {:communities (map :property/internal-name communities)}))

;; =====================================
;; license

(defn- parse-license [data]
  {:license (get-in data [:account/member-application
                          :member-application/desired-license
                          :db/id])})

;; =====================================
;; move-in date

(defn- parse-move-in [data]
  {:move-in-date (get-in data [:account/member-application
                               :member-application/desired-availability])})

;; =====================================
;; Pet

(defn- parse-pet [data]
  (let [{:keys [:pet/type :pet/breed :pet/weight] :as pet}
        (get-in data [:account/member-application
                      :member-application/pet])]
    {:pet (assoc-when {:has-pet (boolean pet)}
                      :pet-type (when type (name type))
                      :breed breed
                      :weight weight)}))

;; =====================================
;; Background

(defn- parse-community-safety [data]
  {:community-safety
   {:consent (get-in data [:community-safety/_account
                           0
                           :community-safety/consent-given?])}})

;; =====================================
;; Address

(defn- parse-address [data]
  (let [address (get-in data [:account/member-application
                              :member-application/current-address])]
    {:address {:city        (:address/city address)
               :state       (:address/state address)
               :postal-code (:address/postal-code address)}}))

;; =====================================
;; Income Files

(defn- parse-income-files [data]
  (let [files (:income-file/_account data)]
    {:income-file-paths (map :income-file/path files)}))

;; =====================================
;; Community Fitness

(defn- parse-community-fitness [data]
  {:community-fitness
   (get-in data [:account/member-application
                 :member-application/community-fitness])})

;; =====================================
;; Completeness

;; NOTE: Using spec for this since it's quick. Might not be the best.
;; I'm really just checking types of the resulting "progress", but the big
;; assumption here is that all of the data was properly validated before going
;; into the database. Since this validation is only of data that's come out of
;; the db, it shouldn't need another serious validation pass.

(s/def ::communities (s/+ string?))
(s/def ::license integer?)
(s/def ::move-in-date :starcity.spec/date)
;; pet
(s/def ::weight integer?)
(s/def ::breed string?)
(s/def ::pet-type #{"cat" "dog"})
(s/def ::has-pet boolean?)
(s/def ::pet (s/keys :req-un [::has-pet] :opt-un [::pet-type ::breed ::weight]))
;; background
(s/def ::consent true?)
(s/def ::community-safety (s/keys :req-un [::consent]))
;; address
(s/def ::not-empty-string (s/and not-empty string?))
(s/def ::city ::not-empty-string)
(s/def ::state ::not-empty-string)
(s/def ::postal-code ::not-empty-string)
(s/def ::address (s/keys :req-un [::city ::state ::postal-code]))

(s/def ::income-file-paths (s/+ string?))
;; Community fitness
(s/def :community-fitness/free-time ::not-empty-string)
(s/def :community-fitness/skills ::not-empty-string)
(s/def :community-fitness/prior-community-housing ::not-empty-string)
(s/def :community-fitness/why-interested ::not-empty-string)
(s/def :community-fitness/dealbreakers ::not-empty-string)
(s/def ::community-fitness
  (s/keys :req [:community-fitness/free-time
                :community-fitness/skills
                :community-fitness/prior-community-housing
                :community-fitness/why-interested]
          :opt [:community-fitness/dealbreakers]))

(s/def ::complete-parsed-data
  (s/keys :req-un [::communities ::license ::move-in-date ::community-safety
                   ::address ::income-file-paths ::community-fitness ::pet]))

(def is-complete?
  (partial s/valid? ::complete-parsed-data))

;; =============================================================================
;; API

(defn progress [account-id]
  (let [data (fetch account-id)
        res  (apply merge ((juxt parse-account
                                 parse-communities
                                 parse-license
                                 parse-move-in
                                 parse-pet
                                 parse-community-safety
                                 parse-address
                                 parse-income-files
                                 parse-community-fitness)
                           data))
        res' (assoc res :complete (is-complete? res))]
    (debug "The progress is: " res')
    res'))

;; =============================================================================
;; Update
;; =============================================================================

;; =============================================================================
;; Internal

(defn- create-application-if-needed
  [account-id]
  (when-not (application/by-account-id account-id)
    @(d/transact conn [{:db/id                       (tempid)
                        :member-application/approved false
                        :account/_member-application account-id}])))

(defmulti ^:private update-tx (fn [_ _ key] key))

(def ^:private communities->lookups
  (partial map (fn [c] [:property/internal-name c])))

(defmethod update-tx :logistics/communities
  [{communities :communities} application _]
  (replace-unique2 conn
                   (:db/id application)
                   :member-application/desired-properties
                   (communities->lookups communities)))

(defmethod update-tx :logistics/license
  [{license :license} application _]
  [[:db/add (:db/id application) :member-application/desired-license license]])

(defmethod update-tx :logistics/move-in-date
  [{date :move-in-date} application _]
  [[:db/add (:db/id application) :member-application/desired-availability (c/to-date date)]])

;; TODO: has-pet? => has-pet

(defmethod update-tx :logistics/pets
  [{:keys [has-pet pet-type breed weight]} application _]
  (let [pet (get application :member-application/pet)]
    (-> (cond
          ;; Indicated that they have a pet AND there's already a pet entiti -- this is an update
          (and has-pet pet)       [(assoc-when {:db/id    (:db/id pet)
                                                :pet/type (keyword pet-type)}
                                               :pet/breed breed :pet/weight weight)]
          ;; Indicated that they do not have a pet, but previously had one. Retract pet entity.
          (and (not has-pet) pet) [[:db.fn/retractEntity (:db/id pet)]]
          ;; Indicated that they have a pet, but previously did not. Create pet entity
          (and has-pet (not pet)) [{:db/id                  (:db/id application)
                                    :member-application/pet (assoc-when {:pet/type (keyword pet-type)}
                                                                        :pet/breed breed :pet/weight weight)}]
          ;; Indicated that they have no pet, and had no prior pet. Do nothing.
          (and (not has-pet) (not pet)) [])
        ;; Update application flag.
        (conj [:db/add (:db/id application) :member-application/has-pet has-pet]))))

(defmethod update-tx :personal/phone-number
  [{phone-number :phone-number} application _]
  [[:db/add (account-id application) :account/phone-number phone-number]])

(defn- consent-tx
  [{consent :consent} application]
  (if-let [cs (community-safety application)]
    [[:db/add (:db/id cs) :community-safety/consent-given? consent]]
    [{:db/id                           (tempid)
      :community-safety/account        (account-id application)
      :community-safety/consent-given? consent}]))

(defn- account-tx
  [{{:keys [first middle last]} :name, dob :dob} application]
  (let [eid (account-id application)]
    [[:db/add eid :account/first-name first]
     (if (nil? middle)
       [:db/add eid :account/middle-name ""]
       [:db/add eid :account/middle-name middle])
     [:db/add eid :account/last-name last]
     [:db/add eid :account/dob (c/to-date dob)]]))

(defn- address-tx
  [{{:keys [state city zip postal-code]} :address} application]
  [{:db/id (:db/id application)
    :member-application/current-address
    {:address/state       state
     :address/city        city
     :address/postal-code (or zip postal-code)}}])

(defmethod update-tx :personal/background
  [data application _]
  (vec (concat (consent-tx data application)
               (account-tx data application)
               (address-tx data application))))

(defn- community-fitness-id
  [application]
  (get-in application [:member-application/community-fitness :db/id]))

(defn- community-fitness-tx
  [application data]
  [(if-let [cfid (community-fitness-id application)]
     ;; Already a community fitness entity -- update it
     (merge {:db/id cfid} data)
     ;; Create a new community fitness entity (it's a component)
     {:db/id                                (:db/id application)
      :member-application/community-fitness data})])

(defmethod update-tx :community/why-starcity
  [{:keys [why-starcity]} app _]
  (community-fitness-tx app {:community-fitness/why-interested why-starcity}))

;; dealbreakers are considered optional, and may be nil
(defmethod update-tx :community/about-you
  [{:keys [free-time dealbreakers]} app _]
  (->> (assoc-when {:community-fitness/free-time free-time}
                   :community-fitness/dealbreakers dealbreakers)
       (community-fitness-tx app)))

(defmethod update-tx :community/communal-living
  [{:keys [prior-experience skills]} app _]
  (->> {:community-fitness/skills                  skills
        :community-fitness/prior-community-housing prior-experience}
       (community-fitness-tx app)))

;; =============================================================================
;; API

(defn update
  [data account-id key]
  (do
    (create-application-if-needed account-id)
    (let [tx-data (update-tx data (application/by-account-id account-id) key)]
      (debug "tx-data" tx-data)
      @(d/transact conn tx-data))))
