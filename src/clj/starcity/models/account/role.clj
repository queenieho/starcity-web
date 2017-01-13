(ns starcity.models.account.role
  (:require [clojure.spec :as s]
            [starcity.util :refer [entity?]]))

(s/def ::role
  #{:account.role/admin
    :account.role/member
    :account.role/onboarding
    :account.role/applicant})

;;; Roles

(def admin :account.role/admin)
(def onboarding :account.role/onboarding)
(def applicant :account.role/applicant)
(def member :account.role/member)

;; Role Hierarchy

(derive admin applicant)
(derive admin member)
(derive admin onboarding)

;;; Predicates

(defn- is-role [role account]
  (= role (:account/role account)))

(s/fdef is-role
        :args (s/cat :role ::role :account entity?)
        :ret boolean?)

(def applicant? (partial is-role applicant))
(def member? (partial is-role member))
(def admin? (partial is-role admin))
(def onboarding? (partial is-role onboarding))

;;; Transactions

(defn change-role [account to]
  {:db/id        (:db/id account)
   :account/role to})

(s/fdef change-role
        :args (s/cat :account entity? :to ::role)
        :ret (s/keys :req [:db/id :account/role]))
