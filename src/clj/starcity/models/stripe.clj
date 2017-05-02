(ns starcity.models.stripe
  (:require [clojure.spec :as s]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when]]
            [starcity.datomic.partition :refer [tempid]]
            [starcity.services.stripe :as service]))

;; TODO: Delete this namespace. Shouldn't be a model.
;; It's currently used in:
;; `starcity.models.apply.submit`
;; `starcity.models.onboarding`

;; =============================================================================
;; Actions

(defn create-charge!
  "Attempt to create a Stripe charge for given `account-id`. Successful creation
  results in creation of a corresponding `charge`, otherwise an exception is thrown."
  [conn account-id amount source & {:keys [description customer-id managed-account]}]
  (let [email (:account/email (d/entity (d/db conn) account-id))
        res   (service/charge amount source email
                              :description description
                              :customer-id customer-id
                              :managed-account managed-account)]
    (if-let [e (service/error-from res)]
      (throw (ex-info "Error encountered while trying to create charge!" e))
      (let [payload   (service/payload-from res)
            tid       (tempid)
            tx        @(d/transact conn [(assoc-when
                                          {:db/id            tid
                                           :charge/stripe-id (:id payload)
                                           :charge/amount    (float (/ amount 100))
                                           :charge/account   account-id
                                           :charge/status    :charge.status/pending}
                                          :charge/purpose description)])
            charge-id (d/resolve-tempid (d/db conn) (:tempids tx) tid)]
        charge-id))))

(defn fetch-charge
  "Fetch a charge from Stripe's servers."
  [{:keys [:charge/stripe-id]}]
  (let [res (service/fetch-charge stripe-id)]
    (if-let [err (service/error-from res)]
      (throw (ex-info "Error encountered while fetching charge!" err))
      (service/payload-from res))))

;; =============================================================================
;; Misc.

;; TODO: Janky? Possible to remove? Only used in one place...
(defn exception-msg
  [e]
  (-> e ex-data :message))
