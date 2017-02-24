(ns starcity.models.apply.update
  (:require [starcity.models.apply.common :as common]
            [starcity.models.util.update :refer [replace-unique]]
            [starcity.datomic :refer [conn tempid]]
            [datomic.api :as d]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [plumbing.core :refer [assoc-when]])
  (:refer-clojure :exclude [update]))

;; =============================================================================
;; Internal
;; =============================================================================

(defn- community-safety [application]
  (-> application :account/_application first :community-safety/_account first))

(defn- create-application-if-needed
  [account-id]
  (when-not (common/by-account-id account-id)
    @(d/transact
      conn
      [{:db/id                (tempid)
        :application/status   :application.status/in-progress
        :account/_application account-id}])))

(defmulti ^:private update-tx (fn [_ _ key] key))

(def ^:private communities->lookups
  (partial map (fn [c] [:property/internal-name c])))

(defmethod update-tx :logistics/communities
  [{communities :communities} application _]
  (replace-unique conn
                  (:db/id application)
                  :application/communities
                  (communities->lookups communities)))

(defmethod update-tx :logistics/license
  [{license :license} application _]
  [[:db/add (:db/id application) :application/license license]])

(defmethod update-tx :logistics/move-in-date
  [{date :move-in-date} application _]
  [[:db/add (:db/id application) :application/move-in (c/to-date date)]])

;; TODO: has-pet? => has-pet

(defmethod update-tx :logistics/pets
  [{:keys [has-pet pet-type breed weight]} application _]
  (let [pet (get application :application/pet)]
    (-> (cond
          ;; Indicated that they have a pet AND there's already a pet entiti -- this is an update
          (and has-pet pet)       [(assoc-when {:db/id    (:db/id pet)
                                                :pet/type (keyword pet-type)}
                                               :pet/breed breed :pet/weight weight)]
          ;; Indicated that they do not have a pet, but previously had one. Retract pet entity.
          (and (not has-pet) pet) [[:db.fn/retractEntity (:db/id pet)]]
          ;; Indicated that they have a pet, but previously did not. Create pet entity
          (and has-pet (not pet)) [{:db/id                  (:db/id application)
                                    :application/pet (assoc-when {:pet/type (keyword pet-type)}
                                                                        :pet/breed breed :pet/weight weight)}]
          ;; Indicated that they have no pet, and had no prior pet. Do nothing.
          (and (not has-pet) (not pet)) [])
        ;; Update application flag.
        (conj [:db/add (:db/id application) :application/has-pet has-pet]))))

(defmethod update-tx :personal/phone-number
  [{phone-number :phone-number} application _]
  [[:db/add (common/account-id application) :account/phone-number phone-number]])

(defn- consent-tx
  [{consent :consent} application]
  (if-let [cs (community-safety application)]
    [[:db/add (:db/id cs) :community-safety/consent-given? consent]]
    [{:db/id                           (tempid)
      :community-safety/account        (common/account-id application)
      :community-safety/consent-given? consent}]))

(defn- account-tx
  [{{:keys [first middle last]} :name, dob :dob} application]
  (let [eid (common/account-id application)]
    [[:db/add eid :account/first-name first]
     (if (nil? middle)
       [:db/add eid :account/middle-name ""]
       [:db/add eid :account/middle-name middle])
     [:db/add eid :account/last-name last]
     [:db/add eid :account/dob (c/to-date dob)]]))

(defn- address-tx
  [{{:keys [region locality country postal-code]} :address} application]
  [{:db/id (:db/id application)
    :application/address
    {:address/region      region
     :address/locality    locality
     :address/country     country
     :address/postal-code postal-code}}])

(defmethod update-tx :personal/background
  [data application _]
  (vec (concat (consent-tx data application)
               (account-tx data application)
               (address-tx data application))))

(defn- community-fitness-id
  [application]
  (get-in application [:application/fitness :db/id]))

(defn- community-fitness-tx
  [application data]
  [(if-let [cfid (community-fitness-id application)]
     ;; Already a community fitness entity -- update it
     (merge {:db/id cfid} data)
     ;; Create a new community fitness entity (it's a component)
     {:db/id               (:db/id application)
      :application/fitness data})])

(defmethod update-tx :community/why-starcity
  [{:keys [why-starcity]} app _]
  (community-fitness-tx app {:fitness/interested why-starcity}))

;; dealbreakers are considered optional, and may be nil
(defmethod update-tx :community/about-you
  [{:keys [free-time dealbreakers]} app _]
  (->> (assoc-when {:fitness/free-time free-time}
                   :fitness/dealbreakers dealbreakers)
       (community-fitness-tx app)))

(defmethod update-tx :community/communal-living
  [{:keys [prior-experience skills]} app _]
  (->> {:fitness/skills     skills
        :fitness/experience prior-experience}
       (community-fitness-tx app)))

;; =============================================================================
;; API
;; =============================================================================

(defn update
  "Update the member's application."
  [data account-id key]
  (do
    (create-application-if-needed account-id)
    (let [tx-data (update-tx data (common/by-account-id account-id) key)]
      @(d/transact conn tx-data))))
