(ns starcity.models.community-safety
  (:require [starcity.services.community-safety :as community-safety]
            [starcity.models.util :refer :all]
            [starcity.datomic :refer [conn tempid]]
            [datomic.api :as d]
            [plumbing.core :refer [assoc-when]]
            [taoensso.timbre :refer [infof warnf errorf]]))

;; =============================================================================
;; Helpers
;; =============================================================================

(defn- fetch-background-data
  "Pull the requisite/availabile information needed to run a background check."
  [account-id]
  (let [pattern [:db/id
                 :account/first-name
                 :account/last-name
                 :account/middle-name
                 :account/dob
                 :account/email
                 {:account/member-application
                  [{:member-application/current-address
                    [:address/region :address/locality :address/postal-code :address/country]}]}]]
    (d/pull (d/db conn) pattern account-id)))

(defn- perform-background-check
  "Perform the background check itself using the service."
  [{:keys [:account/middle-name :account/member-application] :as data}]
  (letfn [(-maybe-assoc-middle-name [opts]
            (if-not (empty? middle-name)
              (assoc opts :middle-name middle-name)
              opts))
          (-maybe-assoc-address [opts]
            (if-let [address (:member-application/current-address data)]
              (assoc opts :address {:city        (:address/locality address)
                                    :state       (:address/region address)
                                    :postal-code (:address/postal-code address)})
              opts))]
    (community-safety/background-check (:db/id data)
                                       (:account/first-name data)
                                       (:account/last-name data)
                                       (:account/email data)
                                       (:account/dob data)
                                       (-> {}
                                           -maybe-assoc-middle-name
                                           -maybe-assoc-address))))

(defmulti log-result (fn [result _] (:status result)))

(defmethod log-result 201 [_ account-id]
  (infof "Successfully ran background check for account-id: %s" account-id))

(defmethod log-result nil [_ account-id]
  (errorf "Background check not performed for account-id: %s" account-id))

(defmethod log-result :default [{:keys [status body]} account-id]
  (warnf "Error running background check! account-id: %s -- error code: %d -- payload: %s"
         account-id status body))

(defn- save-results
  [account-id report-url]
  (let [ent {:community-safety/account        account-id
             :community-safety/report-url    report-url
             :community-safety/wants-report? true}
        tid (tempid)
        tx  @(d/transact conn [(assoc ent :db/id tid)])]
    (d/resolve-tempid (d/db conn) (:tempids tx) tid)))

(def ^:private report-url-from
  (comp :location :headers))

(def ^:private check-successful?
  (comp (partial = 201) :status))

(defn- check-failed? [{:keys [status] :as result}]
  (and (not (nil? result)) ((comp not check-successful?) status)))

;; =============================================================================
;; API
;; =============================================================================

(defn check
  "Perform a community safety check on `account-id`."
  [account-id]
  (let [result (-> account-id
                   fetch-background-data
                   perform-background-check)]
    (log-result result account-id)
    (cond
      (check-successful? result) (save-results account-id (report-url-from result))
      (check-failed? result)     (throw (ex-info "Failed to run background check!" {:account-id account-id}))
      :otherwise                 :noop)))
