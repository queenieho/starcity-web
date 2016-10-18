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

(defn- app-id->acct-id
  "Find the `account-id` for the given member application id (`application-id`)."
  [application-id]
  (:db/id
   (qe1 '[:find ?acct
          :in $ ?app
          :where
          [?acct :account/member-application ?app]]
        (d/db conn) application-id)))

(s/fdef app-id->acct-id
        :args (s/cat :application-id integer?)
        :ret integer?)

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
  [account]
  [{:db/id        account
    :account/role account/onboarding-role}])

(s/fdef update-role-txdata
        :args (s/cat :account-id :starcity.spec/lookup)
        :ret vector?)

;; =============================================================================
;; API
;; =============================================================================

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

(def email-subject
  "Starcity: You've been qualified!")

(defn approve!
  [application-id approver-id internal-name email-content]
  (let [account-id (app-id->acct-id application-id)
        email          (:account/email (d/entity (d/db conn) account-id))
        txdata     (concat
                    ;; Create approval entity
                    (create-txdata account-id
                                   approver-id
                                   [:property/internal-name internal-name])
                    ;; Convert account to new role
                    (update-role-txdata account-id))]
    (do
      ;; Commit the changes
      @(d/transact conn (vec txdata))
      ;; Send email
      (mailgun/send-email email email-subject email-content))))

(s/fdef approve!
        :args (s/cat :application-id integer?
                     :approver-id integer?
                     :internal-name string?
                     :email-content string?))

(comment

  (email-content 285873023222906 "52gilbert")

  )
