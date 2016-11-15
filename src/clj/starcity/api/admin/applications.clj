(ns starcity.api.admin.applications
  (:require [clojure
             [spec :as s]
             [string :as str]]
            [compojure.core :refer [defroutes GET POST]]
            [datomic.api :as d]
            [starcity
             [datomic :refer [conn]]]
            [starcity.util :refer [str->int strip-namespaces]]
            [starcity.api.common :as api]
            [starcity.models
             [account :as account :refer [full-name]]
             [application :as application]
             [approval :as approval]
             [util :refer :all]]
            [starcity.api.admin.applications.list :as list]
            [starcity.api.admin.applications.entry :as entry]))

;; =============================================================================
;; API
;; =============================================================================

;; =============================================================================
;; Approval

(defn- can-approve?
  [application-id]
  (let [application (d/entity (d/db conn) application-id)]
    (and (not (application/approved? application))
         (application/locked? application)
         (account/applicant? (account/by-application application)))))

(def cannot-approve? (comp not can-approve?))

(defn approve
  "Approve the application identified by `application-id`."
  [application-id approver-id internal-name deposit-amount email-content email-subject]
  (if (cannot-approve? application-id)
    (api/unprocessable {:error "This application cannot be approved! This could be because the application belongs to a non-applicant, is not yet complete, or is already approved."})
    (do
      (approval/approve! {:application-id application-id
                          :approver-id    approver-id
                          :internal-name  internal-name
                          :deposit-amount deposit-amount
                          :email-content  email-content
                          :email-subject  email-subject})
      (api/ok {}))))

(s/fdef approve
        :args (s/cat :application-id integer?
                     :approver-id integer?
                     :internal-name string?
                     :deposit-amount integer?
                     :email-content string?
                     :email-subject string?))

;; =============================================================================
;; Routes

(defroutes routes
  (GET "/" [limit offset direction sort-key view q]
       (fn [_]
         (list/fetch (str->int limit)
                     (str->int offset)
                     (keyword direction)
                     (keyword sort-key)
                     (keyword view)
                     q)))

  (GET "/:application-id" [application-id]
       (fn [_] (entry/fetch (str->int application-id))))

  (POST "/:application-id/approve" [application-id]
        (fn [{:keys [params] :as req}]
          (let [{:keys [email-content deposit-amount community-id email-subject]} params]
            (approve (str->int application-id)
                     (api/account-id req)
                     community-id
                     (str->int deposit-amount)
                     email-content
                     email-subject)))))
