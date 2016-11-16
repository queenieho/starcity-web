(ns starcity.models.application
  (:require [starcity.datomic :refer [conn tempid]]
            [starcity.models.util :refer :all]
            [starcity.models.util.update :refer :all]
            [starcity.spec]
            [clojure.spec :as s]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when]]
            [starcity.models.account :as account]))

;; =============================================================================
;; API
;; =============================================================================

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
        :args (s/cat :account-id :starcity.spec/lookup))

;; =============================================================================
;; Predicates

(declare status)

(defn in-progress?
  "Has this application been submitted?"
  [application]
  (= :member-application.status/in-progress (status application)))

(s/fdef in-progress?
        :args (s/cat :application :starcity.spec/entity)
        :ret boolean?)

(defn submitted?
  "Has this application been submitted?"
  [application]
  (= :member-application.status/submitted (status application)))

(s/fdef submitted?
        :args (s/cat :application :starcity.spec/entity)
        :ret boolean?)

(defn approved?
  "Is this application approved?"
  [application]
  (= :member-application.status/approved (status application)))

(s/fdef approved?
        :args (s/cat :application :starcity.spec/entity)
        :ret boolean?)

;; alias for convenience
(def completed? submitted?)

;; =============================================================================
;; Selectors

(defn license
  [application]
  (get-in application [:member-application/desired-license]))

(defn term
  [application]
  (:license/term (license application)))

(def full-name
  "Get the full name of the applicant that this application belongs to."
  (comp account/full-name first :account/_member-application))

(def move-in-date :member-application/desired-availability)
(def communities :member-application/desired-properties)
(def community-fitness :member-application/community-fitness)
(def address :member-application/current-address)
(def has-pet? :member-application/has-pet)
(def pet :member-application/pet)
(def completed-at :member-application/submitted-at)
(def status :member-application/status)
