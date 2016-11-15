(ns starcity.models.approval
  (:require [starcity.models.account :as account]
            [starcity.models.util :refer :all]
            [starcity.datomic :refer [conn tempid]]
            [starcity.services.mailgun :as mailgun]
            [hiccup.core :refer [html]]
            [datomic.api :as d]
            [starcity.spec]
            [clojure.spec :as s]))

;; =============================================================================
;; Internal
;; =============================================================================

;; =============================================================================
;; Transactions

(defn- create-txdata
  "Produce the transaction data to create a new approval entity."
  [account-id approver-id property-id]
  [{:db/id                (tempid)
    :approval/account     account-id
    :approval/approved-by approver-id
    :approval/approved-on (java.util.Date.)
    :approval/property    property-id}])

(s/fdef create-txdata
        :args (s/cat :account-id :starcity.spec/lookup
                     :approved-by-id :starcity.spec/lookup
                     :property-id :starcity.spec/lookup)
        :ret vector?)

(defn- update-role-txdata
  "Produce the transaction data required to mark this user's account as approved
  by updating his/her role."
  [account-id]
  [{:db/id        account-id
    :account/role account/onboarding-role}])

(s/fdef update-role-txdata
        :args (s/cat :account-id :starcity.spec/lookup)
        :ret vector?)

(defn- create-security-deposit-txdata
  [account-id deposit-amount]
  [{:db/id                            (tempid)
    :security-deposit/account         account-id
    :security-deposit/amount-required deposit-amount}])

(s/fdef create-security-deposit-txdata
        :args (s/cat :account-id :starcity.spec/lookup
                     :deposit-amount integer?)
        :ret vector?)

;; =============================================================================
;; API
;; =============================================================================

;; =============================================================================
;; Queries

(defn by-application-id
  "Retrieve the approval entity for a given `application-id`."
  [application-id]
  (qe1 '[:find ?approval
         :in $ ?application
         :where
         [?account :account/member-application ?application]
         [?approval :approval/account ?account]]
       (d/db conn) application-id))

(s/fdef by-application-id
        :args (s/cat :application-id :starcity.spec/lookup)
        :ret integer?)

;; =============================================================================
;; Actions

;; NOTE: A weird bug prevented me from calling this function with five arguments
;; -- I have no clue why. Converting the args to a map seems to have fixed it,
;; although not sure why. Shrug.

(defn approve!
  [{:keys [application-id approver-id internal-name deposit-amount email-content email-subject]}]
  (let [account (account/by-application (d/entity (d/db conn) application-id))
        email   (:account/email account)
        txdata  (concat
                 ;; Create approval entity
                 (create-txdata (:db/id account)
                                approver-id
                                [:property/internal-name internal-name])
                 ;; Convert account to new role
                 (update-role-txdata (:db/id account))
                 ;; Create security deposit entity
                 (create-security-deposit-txdata (:db/id account) deposit-amount))]
    (do
      ;; Commit the changes
      @(d/transact conn (vec txdata))
      ;; Send email
      (mailgun/send-email email email-subject email-content))))

(s/fdef approve!
        :args (s/cat :args
                     (s/keys :req-un [::application-id
                                      ::approver-id
                                      ::internal-name
                                      ::deposit-amount
                                      ::email-content])))
